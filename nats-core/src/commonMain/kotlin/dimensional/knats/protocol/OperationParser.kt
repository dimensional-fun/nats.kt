package dimensional.knats.protocol

import dimensional.knats.connection.NatsException
import dimensional.knats.tools.*
import io.ktor.utils.io.core.*
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import naibu.ext.ktor.io.ktor
import naibu.io.SmallMemoryPool
import naibu.io.slice.get
import naibu.logging.logging
import naibu.serialization.DefaultFormats
import naibu.serialization.deserialize
import naibu.serialization.json.Json
import naibu.text.charset.decodeIntoString
import kotlin.time.measureTimedValue

public class OperationParser : SynchronizedObject() {
    public companion object {
        private val log by logging { }

        private fun Input.ensureCRLF(buffer: ByteArray = ByteArray(2)) {
            readFully(buffer)
            require(buffer eq CRLF) { "Didn't receive CRLF" }
            buffer.fill(0)
        }

        private fun <T> Input.ensureCRLF(buffer: ByteArray = ByteArray(2), block: () -> T) = try {
            block()
        } finally {
            ensureCRLF(buffer)
        }

        private fun Input.readUntilCRLF(): ByteReadPacket = buildPacket {
            readUntilDelimiters(CR, LF, this)
        }

        private fun Char.estimateOpLength(): Int? = when (this) {
            'M', '+' -> 3
            'I', 'H', 'P', '-' -> 4
            'C' -> 7
            else -> null
        }
    }

    private val crlfBuff = ByteArray(2)
    private val opBuffer = SmallMemoryPool.take()

    public fun parse(packet: ByteReadPacket): Operation? = synchronized(this) {
        val (op, took) = measureTimedValue { parseInner(packet) }
        log.trace { "Took $took to parse ${op?.tag ?: "nothing lol"}" }

        return op
    }

    private fun parseInner(packet: ByteReadPacket): Operation? {
        if (packet.isEmpty) {
            return null
        }

        /* peek the first byte (it won't be -1 since we check whether the packet is empty)
           & estimate the number of bytes to read for the OP name */
        val opLength = packet.tryPeek()
            .toChar()
            .estimateOpLength()
            ?: return null

        /* read the estimated number of bytes into the op buffer & skip all whitespace characters */
        packet.readFully(opBuffer.ktor(), 0, opLength)
        packet.discardValues(WHITESPACE)

        /* parse the different ops */
        val op = when (val opName = opBuffer[0..<opLength].decodeIntoString().uppercase()) {
            "INFO" -> {
                val options = packet.readUntilCRLF()
                    .readText()
                    .deserialize<NatsInfoOptions>(DefaultFormats.Json)

                Operation.Info(options)
            }

            "MSG" -> {
                // read arguments.
                val args = packet
                    .readUntilCRLF()
                    .readText()
                    .split('\t', ' ')

                val builder = Operation.Msg.Builder()
                builder.subject = args[0]
                builder.sid = args[1]
                if (args.size == 4) {
                    builder.replyTo = args[2]
                }

                packet.ensureCRLF(crlfBuff)

                // read payload.
                val bytes = args.last().toInt()
                if (bytes != 0) {
                    builder.payload = ByteReadPacket(packet.readBytes(n = bytes))
                }

                builder.build()
            }

            "HMSG" -> {
                val builder = packet.ensureCRLF {
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

                val version = packet.ensureCRLF(crlfBuff) {
                    packet.readUntilCRLF()
                }

                var read = version.remaining + 2
                while (read < builder.hdrLen - 2) {
                    val header = packet.ensureCRLF(crlfBuff) { packet.readUntilCRLF() }
                    read += header.remaining + 2

                    val name = header.readUntilDelimiter(COLON)
                    if (header.readByte() != COLON) {
                        throw NatsException.ProtocolException("Expected ':' after header name")
                    }

                    header.discardValues(WHITESPACE)
                    builder.headers.append(name.readText(), header.readText())
                }

                packet.ensureCRLF(crlfBuff)

                /* read payload len */
                val payloadLength = builder.totLen - read - 2
                builder.payload = payloadLength.toInt().takeIf { it > 0 }
                    ?.let(packet::readBytes)
                    ?.let(::ByteReadPacket)

                builder.build()
            }

            "PING", "PONG" -> {
                if (opName == "PING") Operation.Ping else Operation.Pong
            }

            "+OK" -> {
                Operation.Ok
            }

            "-ERR" -> {
                val message = packet
                    .readUntilCRLF()
                    .readText()

                Operation.Err(message)
            }

            else -> throw NatsException.ProtocolException("Unknown operation: $opName")
        }

        packet.ensureCRLF(crlfBuff)
        return op
    }
}
