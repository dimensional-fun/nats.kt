package dimensional.knats.protocol.impl

import dimensional.knats.internal.NatsException
import dimensional.knats.protocol.*
import dimensional.knats.tools.COLON
import dimensional.knats.tools.Json
import dimensional.knats.tools.WHITESPACE
import dimensional.knats.tools.ktor.discardValues
import dimensional.knats.tools.ktor.readFully
import dimensional.knats.tools.ktor.readUntilDelimiter
import dimensional.knats.tools.ktor.tryPeek
import io.ktor.utils.io.*
import naibu.common.pool.use
import naibu.io.SmallMemoryPool
import naibu.io.slice.get
import naibu.logging.logging
import naibu.serialization.DefaultFormats
import naibu.serialization.deserialize
import naibu.text.charset.decodeIntoString
import kotlin.collections.MutableMap
import kotlin.collections.last
import kotlin.collections.lastIndex
import kotlin.collections.mutableMapOf
import kotlin.collections.set

public open class DefaultOperationParser : OperationParser {
    public companion object : DefaultOperationParser() {
        private val log by logging { }

        public const val MAX_OP_NAME_LENGTH: Int = 7
    };

    private val opDecoders: MutableMap<String, suspend (ByteReadChannel) -> Operation> = mutableMapOf()

    init {
        addDecoder("INFO") {
            it.discardValues(WHITESPACE)

            val options = it.readUntilCRLF()
                .readText()
                .deserialize<NatsInfoOptions>(DefaultFormats.Json)

            Operation.Info(options)
        }

        addDecoder("MSG") {
            it.discardValues(WHITESPACE)

            // read arguments.
            val args = it
                .readUntilCRLF()
                .readText()
                .split('\t', ' ')

            val builder = Operation.Msg.Builder()
            builder.subject = args[0]
            builder.sid = args[1]
            if (args.size == 4) {
                builder.replyTo = args[2]
            }

            it.ensureCRLF()

            // read payload.
            val payloadSize = args.last().toInt()
            if (payloadSize != 0) {
                builder.payload = it.readPacket(payloadSize)
            }

            builder.build()
        }

        addDecoder("HMSG") { packet ->
            packet.discardValues(WHITESPACE)

            val builder = packet.ensureCRLF {
                /* read initial headers */
                val args = packet
                    .readUntilCRLF()
                    .readText()
                    .split('\t', ' ')

                val builder = Operation.MsgWithHeaders.Builder()
                builder.subject = args[0]
                builder.sid = args[1]
                if (args.size == 5) {
                    builder.replyTo = args[2]
                }

                builder.hdrLen = args[args.lastIndex - 1].toInt()
                builder.totLen = args.last().toInt()
                builder
            }

            /* read version, e.g., NATS/1.0 */
            val version = packet.ensureCRLF {
                packet.readUntilCRLF()
            }

            builder.version = version.copy().readText()

            /* read headers */
            packet.ensureCRLF {
                var read = version.remaining + 4
                while (read < builder.hdrLen) {
                    val header = packet.ensureCRLF {
                        packet.readUntilCRLF()
                    }

                    read += header.remaining + 2

                    val name = header.readUntilDelimiter(COLON)
                    if (header.readByte() != COLON) {
                        throw NatsException.ProtocolException("Expected ':' after header name")
                    }

                    header.discardValues(WHITESPACE)
                    builder.headers.append(name.readText(), header.readText())
                }
            }

            /* read payload len */
            val payloadLength = builder.totLen - builder.hdrLen
            builder.payload = payloadLength.takeIf { it > 0 }
                ?.let { len -> packet.readPacket(len) }

            builder.build()
        }

        addDecoder("PING") {
            Operation.Ping
        }

        addDecoder("PONG") {
            Operation.Pong
        }

        addDecoder("+OK") {
            Operation.Ok
        }

        addDecoder("-ERR") {
            it.discardValues(WHITESPACE)

            val message = it
                .readUntilCRLF()
                .readText()

            Operation.Err(message)
        }
    }

    override suspend fun parse(channel: ByteReadChannel): Operation {
        val opName = channel.readOpName()
        log.debug { "Read op name: $opName" }

        //
        val dec = opDecoders[opName]
            ?: throw NatsException.ProtocolException("Unknown operation: $opName")

        return channel.ensureCRLF {
            // even though parse() is a suspending function & ensureCRLF is inline
            // it doesn't allow us to pass a suspending function.
            dec(it)
        }
    }

    protected fun addDecoder(op: String, block: suspend (packet: ByteReadChannel) -> Operation) {
        opDecoders[op] = block
    }

    private suspend fun ByteReadChannel.readOpName(): String = SmallMemoryPool.use { opBuffer ->
        /* peek the first byte estimate the number of bytes to read for the OP name */
        val opLength = tryPeek().estimateOpLength()
            ?: throw NatsException.ProtocolException("Unable to estimate operation name length")

        readFully(opBuffer, 0, opLength)

        /* only use `opLength` bytes from the buffer when decoding */
        opBuffer[0..<opLength].decodeIntoString().uppercase()
    }
}
