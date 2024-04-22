package nats.core.protocol

import nats.core.NatsException
import nats.core.tools.COLON
import nats.core.tools.Json
import nats.core.tools.WHITESPACE
import nats.core.tools.ktor.discardValues
import nats.core.tools.ktor.readFully
import nats.core.tools.ktor.readUntilDelimiter
import nats.core.tools.ktor.tryPeek
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import naibu.common.pool.use
import naibu.io.SmallMemoryPool
import naibu.io.slice.get
import naibu.serialization.DefaultFormats
import naibu.serialization.deserialize
import naibu.text.charset.decodeIntoString

/**
 * The default operation parser, uses a [ByteReadChannel] and its suspending capabilities to parse the NATS protocol
 * without lots of state.
 */
public open class DefaultOperationParser : OperationParser {
    public companion object : DefaultOperationParser();

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
            builder.subject = Subject(args[0])
            builder.sid = args[1]
            if (args.size == 4) {
                builder.replyTo = Subject(args[2])
            }

            it.ensureCRLF()

            // read payload.
            val payloadSize = args.last().toInt()
            if (payloadSize != 0) {
                builder.payload = it.readPacket(payloadSize)
            }

            builder.build()
        }

        addDecoder("HMSG") { channel ->
            channel.discardValues(WHITESPACE)

            val builder = channel.ensureCRLF {
                /* read initial headers */
                val args = channel
                    .readUntilCRLF()
                    .readBytes()
                    .decodeIntoString()
                    .split('\t', ' ')

                val builder = Operation.MsgWithHeaders.Builder()
                builder.subject = Subject(args[0])
                builder.sid = args[1]
                if (args.size == 5) {
                    builder.replyTo = Subject(args[2])
                }

                builder.hdrLen = args[args.lastIndex - 1].toInt()
                builder.totLen = args.last().toInt()
                builder
            }

            /* read version, e.g., NATS/1.0 */
            val version = channel.ensureCRLF {
                channel.readUntilCRLF()
            }

            builder.version = version.copy().readText()

            /* read headers */
            var read = version.remaining + 4
            while (read < builder.hdrLen) {
                val name = channel.readUntilDelimiter(COLON)
                if (channel.readByte() != COLON) {
                    throw NatsException.ProtocolException("Expected ':' after header name")
                }

                read += name.remaining + 1 + channel.discardValues(WHITESPACE)

                /* read header value & update amount of read bytes. */
                val value = channel.ensureCRLF { channel.readUntilCRLF() }
                read += value.remaining + 2

                /* */
                builder.headers.append(name.readText(), value.readText())
            }

            channel.ensureCRLF()

            /* read payload len */
            builder.payload = (builder.totLen - builder.hdrLen).takeIf { it > 0 }
                ?.let { len -> channel.readPacket(len) }

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

    override fun toString(): String = "DefaultOperationParser(ops=${opDecoders.keys})"
}