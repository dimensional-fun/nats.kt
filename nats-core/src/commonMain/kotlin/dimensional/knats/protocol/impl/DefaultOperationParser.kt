package dimensional.knats.protocol.impl

import dimensional.knats.connection.NatsException
import dimensional.knats.protocol.*
import dimensional.knats.tools.COLON
import dimensional.knats.tools.Json
import dimensional.knats.tools.WHITESPACE
import dimensional.knats.tools.ktor.discardValues
import dimensional.knats.tools.ktor.readFully
import dimensional.knats.tools.ktor.readUntilDelimiter
import dimensional.knats.tools.ktor.tryPeek
import io.ktor.utils.io.*
import naibu.ext.print
import naibu.io.SmallMemoryPool
import naibu.io.slice.get
import naibu.serialization.DefaultFormats
import naibu.serialization.deserialize
import naibu.text.charset.decodeIntoString
import kotlin.time.measureTimedValue

public open class DefaultOperationParser : OperationParser {
    public companion object : DefaultOperationParser();

    private val opDecoders: MutableMap<String, suspend (ByteReadChannel) -> Operation?> = mutableMapOf()

    protected val MAX_OP_NAME_LENGTH: Long get() = 7L

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
            val bytes = args.last().toInt()
            if (bytes != 0) {
                builder.payload = it.readPacket(bytes)
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

            packet.ensureCRLF()

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

    override suspend fun parse(ch: ByteReadChannel): Operation? = measureTimedValue {
        /* parse the different ops */
        val opName = parseOperationName(ch)
            ?: return null

        /* parse the operation. */
        val dec = opDecoders[opName]
            ?: throw NatsException.ProtocolException("Unknown operation: $opName")

        ch.ensureCRLF {
            // even though parse() is a suspending function & ensureCRLF is inline
            // it doesn't allow us to pass a suspending function.
            dec(it)
        }
    }.also { it.duration.print() }.value

    protected fun addDecoder(op: String, block: suspend (packet: ByteReadChannel) -> Operation?) {
        opDecoders[op] = block
    }

    protected suspend fun parseOperationName(ch: ByteReadChannel): String? {
        val opBuffer = SmallMemoryPool.take()

        /* peek the first byte (it doesn't really matter if it returns -1)
          & estimate the number of bytes to read for the OP name */
        val opLength = ch.tryPeek()
            .toChar()
            .estimateOpLength()
            ?: return null

        /* read the estimated number of bytes into the op buffer */
        ch.readFully(opBuffer, 0, opLength)

        /* create a slice w/ then length of the op & decode it as a string */
        return opBuffer[0..<opLength].decodeIntoString().uppercase()
    }
}
