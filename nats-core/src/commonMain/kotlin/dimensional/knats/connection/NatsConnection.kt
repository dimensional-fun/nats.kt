package dimensional.knats.connection

import dimensional.knats.protocol.NatsConnectOptions
import dimensional.knats.protocol.Operation
import dimensional.knats.protocol.OperationParser
import dimensional.kyuso.Kyuso
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import naibu.ext.intoOrNull
import naibu.logging.logging
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

// TODO: refactor connection state, it's pretty bad lol

public class NatsConnection {
    public companion object {
        private val log by logging { }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName("Connection"))
    private val kyuso = Kyuso(scope)

    private val mutableState = MutableStateFlow<NatsConnectionState>(NatsConnectionState.Disconnected)
    private val mutableLatency = MutableStateFlow<Duration?>(null)

    /**
     * The operations sent by the NATS server.
     */
    public val operations: MutableSharedFlow<Operation> = MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE)

    /**
     * The current state of this NATS connection.
     */
    public val state: StateFlow<NatsConnectionState> = mutableState.asStateFlow()

    public suspend fun send(op: Operation) {
        val state = requireNotNull(state.value.intoOrNull<NatsConnectionState.Active>()) {
            "This connection is not running."
        }

        state.conn.output.writeOperation(op)
    }

    public fun disconnect() {
        require(state.value is NatsConnectionState.Connected || state.value is NatsConnectionState.Running) {
            "This NATS connection is not connected or has been detached."
        }
    }

    public suspend fun connect() {
        require(state.value is NatsConnectionState.Disconnected) {
            "This connection has already been connected."
        }

        val connection = aSocket(SelectorManager(scope.coroutineContext))
            .tcp()
            .connect(InetSocketAddress("154.53.33.228", 4222))
            .connection()

        log.debug { "Connected to ${connection.socket.remoteAddress}" }

        mutableState.update { NatsConnectionState.Connected(connection) }

        //
        var lastPing: TimeMark? = null
        start { op ->
            when (op) {
                is Operation.Info -> {
                    val connect = Operation.Connect(NatsConnectOptions.DEFAULT)
                    connection.output.writeOperation(connect)
                }

                is Operation.Ping -> {
                    lastPing = TimeSource.Monotonic.markNow()
                    connection.output.writeOperation(Operation.Pong)
                }

                is Operation.Pong -> {
                    val latency = lastPing?.elapsedNow()
                    mutableLatency.emit(latency)
                    log.debug { "Latency has been updated: ${latency ?: "N/A"}" }
                }

                is Operation.Err -> {
                    val exp = NatsException.fromErr(op)
                    log.warn(exp) { "Received a protocol exception:" }
                }

                else -> operations.emit(op)
            }
        }
    }

    private fun start(block: suspend (Operation) -> Unit) {
        /* make sure this connection is active. */
        val (conn) = requireNotNull(state.value as? NatsConnectionState.Connected) {
            "This connection has been detached or is already running."
        }

        val task = kyuso.dispatch {
            conn.input.readOperations(block)
        }

        mutableState.update { NatsConnectionState.Running(conn, task) }
    }

    private suspend fun ByteWriteChannel.writeOperation(operation: Operation) {
        log.debug { "<<< $operation" }
        writePacket { operation.encode(this) }
        flush()
    }

    private suspend fun ByteReadChannel.readNextPacket(): ByteReadPacket {
        awaitContent()
        return readPacket(availableForRead)
    }

    private suspend fun ByteReadChannel.readOperations(block: suspend (Operation) -> Unit) {
        val parser = OperationParser()
        while (currentCoroutineContext().isActive && !isClosedForRead) {
            /* read the next packet from the channel. */
            val packet: ByteReadPacket = readNextPacket()

            try {
                /* parse the packet into a NATS operation */
                val operation = parser.parse(packet)
                if (operation == null) {
                    packet.release()
                    continue
                }

                /* emit it to the psyche ward */
                log.debug { ">>> $operation" }
                block(operation)
            } catch (ex: Throwable) {
                packet.release()
                log.warn(ex) { "Encountered an exception while handling operation:" }
            }
        }
    }
}
