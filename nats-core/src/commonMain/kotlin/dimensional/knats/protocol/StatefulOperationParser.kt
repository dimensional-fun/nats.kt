package dimensional.knats.protocol

import dimensional.knats.NatsException
import dimensional.knats.protocol.StatefulOperationParser.State.Active.OP.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import naibu.EmptyByteArray
import naibu.ext.into
import naibu.ext.size
import naibu.io.slice.ByteArraySlice
import naibu.io.slice.Slice
import naibu.io.slice.get
import naibu.math.toIntSafe
import naibu.text.charset.Charsets
import naibu.text.charset.decodeIntoString

/**
 * A stateful operation parser... wip explanation
 */
public class StatefulOperationParser : OperationParser {
    private val mutex = Mutex()
    private var state: State = State.Stopped

    private var argBuf: ByteArraySlice? = null
    private var hdr: Int = -1
    private var sas: Int = -1
    private var drop: Int = -1
    private var msgCopied = false
    private var msgBuf: ByteArraySlice? = null
    private val msgArg = MsgArg()

    override suspend fun parse(channel: ByteReadChannel): Operation = mutex.withLock {
        if (state == State.Stopped) {
            val packet = channel
                .readRemaining(channel.availableForRead.toLong())
                .readBytes()

            state = State.Active(packet)
        }

        val state = state.into<State.Active>()
        fun error(): Nothing = throw NatsException.ProtocolException("Parse Error [${state.op}]")

        var i = 0
        var operation: Operation? = null
        while (i in state.packet.indices) {
            val b = state.packet[i++]
            val c = b.toInt().toChar()
            when (state.op) {
                OP_START -> when (c) {
                    'M', 'm' -> {
                        state.op = OP_M
                        this.msgArg.hdr = -1
                        this.hdr = -1
                    }

                    'H', 'h' -> {
                        state.op = OP_H
                        this.msgArg.hdr = 0
                        this.hdr = 0
                    }

                    'I', 'i' -> state.op = OP_I
                    'P', 'p' -> state.op = OP_P
                    '+' -> state.op = OP_PLUS
                    '-' -> state.op = OP_MINUS

                    else -> error()
                }

                OP_H -> when (c) {
                    'M', 'm' -> state.op = OP_M
                    else -> error()
                }

                OP_M -> when (c) {
                    'S', 's' -> state.op = OP_MS
                    else -> error()
                }

                OP_MS -> when (c) {
                    'G', 'g' -> state.op = OP_MSG
                    else -> error()
                }

                OP_MSG -> when (c) {
                    ' ', '\t' -> state.op = OP_MSG_SPC
                    else -> error()
                }

                OP_MSG_SPC -> when (c) {
                    ' ', '\t' -> continue
                    else -> {
                        state.op = MSG_ARG
                        this.sas = i
                    }
                }

                MSG_ARG -> when (c) {
                    '\r' -> this.drop = 1
                    '\n' -> {
                        val arg = argBuf ?: state.packet[this.sas..(i - this.drop)]
                        processMsgArgs(arg)

                        this.drop = 0
                        this.sas = i + 1
                        state.op = MSG_PAYLOAD

                        i = this.sas + msgArg.size - 1
                    }

                    else -> if (argBuf != null) {
                        argBuf = argBuf?.append(b)
                    }
                }

                MSG_PAYLOAD -> msgBuf?.let { msgBuf ->
                    if (msgBuf.size >= msgArg.size) {
                        processMsg(state, msgBuf) // somehow return operation.
                        this.argBuf = null
                        this.msgBuf = null
                        this.msgCopied = false
                        this.state = State.Stopped
                    } else {

                    }
                } ?: if (i - this.sas >= msgArg.size) {

                } else Unit

                MSG_END -> when (c) {
                    '\n' -> {
                        this.drop = 0
                        this.sas = i + 1
                        this.state = State.Stopped
                    }

                    else -> continue
                }

                OP_PLUS -> when (c) {
                    'O', 'o' -> state.op = OP_PLUS_O
                    else -> error()
                }

                OP_PLUS_O -> when (c) {
                    'K', 'k' -> state.op = OP_PLUS_OK
                    else -> error()
                }

                OP_MINUS -> when (c) {
                    'E', 'e' -> state.op = OP_MINUS_E
                    else -> error()
                }

                OP_MINUS_E -> when (c) {
                    'R', 'r' -> state.op = OP_MINUS_ER
                    else -> error()
                }

                OP_MINUS_ER -> when (c) {
                    'R', 'r' -> state.op = OP_MINUS_ERR
                    else -> error()
                }

                OP_MINUS_ERR -> when (c) {
                    ' ', '\t' -> state.op = OP_MINUS_ERR_SPC
                    else -> error()
                }

                OP_MINUS_ERR_SPC -> when (c) {
                    ' ', '\t' -> continue
                    else -> {
                        state.op = MINUS_ERR_ARG
                        this.sas = i
                    }
                }

                MINUS_ERR_ARG -> when (c) {
                    '\r' -> this.drop = 1
                    '\n' -> {

                    }
                }

                else -> error()
            }
        }

        Operation.Ping
    }

    private fun processMsg(state: State.Active, data: ByteArraySlice) {
        val builder = DeliveryBuilder()

        /* decode subject & reply to arguments into strings. */
        builder.subject = msgArg.subject.decodeIntoString(Charsets.US_ASCII)
        builder.replyTo = msgArg.reply?.decodeIntoString(Charsets.US_ASCII)

        /*  */
        val msgPayload = data.copyToArray()

        if (msgArg.hdr > 0) {
        }


        // TODO: process messages.

    }

    private fun processMsgArgs(arg: ByteArraySlice) {
        if (this.hdr >= 0) {
            return processHeaderMsgArgs(arg)
        }

        val args = mutableListOf<ByteArraySlice>()
        var start = -1L
        for (i in arg.indices) {
            val b = arg[i]
            when (b.toInt().toChar()) {
                ' ', '\t', '\r', '\n' -> if (start >= 0) {
                    args += arg[start..i].into<ByteArraySlice>()
                }

                else -> if (start < 0) {
                    start = i
                }
            }
        }

        if (start >= 0) {
            args += (arg + start).into<ByteArraySlice>()
        }

        when (args.size) {
            3 -> {
                msgArg.subject = args[0]
                msgArg.sid = args[1].parseInt64()
                msgArg.reply = null
                msgArg.size = args[2].parseInt64().toInt()
            }

            4 -> {
                msgArg.subject = args[0]
                msgArg.sid = args[1].parseInt64()
                msgArg.reply = args[2]
                msgArg.size = args[3].parseInt64().toInt()
            }

            else -> throw NatsException.ProtocolException("[MSG] Parse Error: $arg")
        }

        if (msgArg.sid < 0) {
            throw NatsException.ProtocolException("[MSG] Bad or Missing Sid: $arg")
        }

        if (msgArg.size < 0) {
            throw NatsException.ProtocolException("[MSG] Bad Or Missing Size: $arg")
        }
    }

    private fun processHeaderMsgArgs(arg: ByteArraySlice) {
        val args = mutableListOf<ByteArraySlice>()
        var start = -1L
        for (i in arg.indices) {
            val b = arg[i]
            when (b.toInt().toChar()) {
                ' ', '\t', '\r', '\n' -> if (start >= 0) {
                    args += arg[start..i].into<ByteArraySlice>()
                }

                else -> if (start < 0) {
                    start = i
                }
            }
        }

        if (start >= 0) {
            args += (arg + start).into<ByteArraySlice>()
        }

        when (args.size) {
            4 -> {
                msgArg.subject = args[0]
                msgArg.sid = args[1].parseInt64()
                msgArg.reply = null
                msgArg.hdr = args[2].parseInt64().toInt()
                msgArg.size = args[3].parseInt64().toInt()
            }

            5 -> {
                msgArg.subject = args[0]
                msgArg.sid = args[1].parseInt64()
                msgArg.reply = args[2]
                msgArg.hdr = args[3].parseInt64().toInt()
                msgArg.size = args[4].parseInt64().toInt()
            }

            else -> throw NatsException.ProtocolException("[HMSG] Parse Error: $arg")
        }

        if (msgArg.sid < 0) {
            throw NatsException.ProtocolException("[HMSG] Bad or Missing Sid: $arg")
        }

        if (msgArg.hdr < 0 || msgArg.hdr > msgArg.size) {
            throw NatsException.ProtocolException("[HMSG] Bad or Missing Header Size: $arg")
        }

        if (msgArg.size < 0) {
            throw NatsException.ProtocolException("[HMSG] Bad or Missing Size: $arg")
        }
    }

    public fun Slice.parseInt64(): Long {
        if (size == 0L) return -1

        var n = 0L
        for (byte in this) {
            val char = byte.toInt().toChar()
            if (char !in '0'..'9') return -1
            n = n * 10 + (char.digitToInt() - '0'.code)
        }

        return n
    }

    private data class MsgArg(
        var subject: ByteArraySlice = EmptyByteArraySlice,
        var reply: ByteArraySlice? = null,
        var sid: Long = -1,
        var hdr: Int = -1,
        var size: Int = -1,
    ) {
        fun reset() {
            subject = EmptyByteArraySlice
            reply = null
            sid = -1
            hdr = -1
            size = -1
        }

        companion object {
            val EmptyByteArraySlice = ByteArraySlice(EmptyByteArray, LongRange.EMPTY)
        }
    }

    private sealed interface State {
        data object Stopped : State

        data class Active(
            val packet: ByteArray,
            var op: OP = OP_START,
        ) : State {
            enum class OP {
                OP_START,

                //
                OP_PLUS,
                OP_PLUS_O,
                OP_PLUS_OK,

                //
                OP_MINUS,
                OP_MINUS_E,
                OP_MINUS_ER,
                OP_MINUS_ERR,
                OP_MINUS_ERR_SPC,
                MINUS_ERR_ARG,

                //
                OP_M,
                OP_MS,
                OP_MSG,
                OP_MSG_SPC,
                MSG_ARG,
                MSG_PAYLOAD,
                MSG_END,

                //
                OP_H,
                OP_P,
                OP_PI,
                OP_PIN,
                OP_PING,
                OP_PO,
                OP_PON,
                OP_PONG,

                //
                OP_I,
                OP_IN,
                OP_INF,
                OP_INFO,
                OP_INFO_SPC,
                INFO_ARG
                ;
            }
        }
    }

    public companion object {
        private val ByteArraySlice.packet: ByteReadPacket
            get() = ByteReadPacket(array, range.first.toIntSafe(), range.size.toIntSafe())

        private inline fun <reified T : Slice> T.append(value: Byte): T {
            val last = size

            val new = resize(range.first..range.last + 1)
            new[last] = value

            return new.into()
        }
    }
}
