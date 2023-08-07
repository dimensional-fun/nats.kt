package dimensional.knats.connection

import dimensional.knats.annotations.InternalNatsApi
import dimensional.knats.client.ClientResources
import dimensional.knats.protocol.Operation
import dimensional.knats.tools.child
import dimensional.knats.transport.Transport
import dimensional.knats.transport.readOperation
import dimensional.knats.transport.write
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import naibu.ext.intoOrNull
import naibu.logging.logging
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@OptIn(InternalNatsApi::class)
internal class ConnectionImpl(private val resources: ClientResources) : Connection {
    companion object {
        private val log by logging { }
    }

    /** Used for connecting to different NATS servers. */
    private val connector = Connector(resources)
    override val scope: CoroutineScope = connector.scope.child(CoroutineName("Connection"))

    //
    private val mutableState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val state: StateFlow<ConnectionState> = mutableState.asStateFlow()

    //
    private val mutableLatency = MutableStateFlow<Duration?>(null)
    override val latency: StateFlow<Duration?> = mutableLatency.asStateFlow()

    //
    private val mutableOperations = MutableSharedFlow<Operation>(extraBufferCapacity = Int.MAX_VALUE)
    override val operations: SharedFlow<Operation> = mutableOperations

    override suspend fun send(operation: Operation, vararg operations: Operation) {
        val (ts) = requireNotNull(state.value.intoOrNull<ConnectionState.Connected>()) {
            "This NATS connection is not connected or has been detached."
        }

        ts.write(operation, *operations)
        ts.flush()
    }

    override suspend fun connect() {
        val (ts, server) = connector.connect()
        val reader = scope.launch { ts.start() }
        mutableState.update { ConnectionState.Connected(ts, server, reader) }
    }

    override suspend fun disconnect() {
        val (ts, _, reader) = requireNotNull(state.value.intoOrNull<ConnectionState.Connected>()) {
            "This NATS connection is not connected or has been detached."
        }

        /* cancel the operation reader & close the transport. */
        reader.cancel()
        ts.close()

        /*  */
        mutableState.update { ConnectionState.Disconnected }
    }

    override suspend fun detach() {
        try {
            disconnect()
        } finally {
            scope.cancel()
            mutableState.update { ConnectionState.Detached }
        }
    }

    private suspend fun Transport.start() = coroutineScope {
        var last: TimeMark? = null
        launch {
            while (isActive) {
                delay(30.seconds)
                if (last != null) {
                    // TODO: handle missed pongs?
                }

                last = TimeSource.Monotonic.markNow()
                send(Operation.Ping)
            }
        }

        readOperations {
            when (it) {
                /* handle server Pings */
                Operation.Ping -> send(Operation.Pong)

                /* handle Ping acknowledgements. */
                Operation.Pong -> {
                    val latency = last?.elapsedNow()
                        ?: return@readOperations

                    log.debug { "Received PING acknowledgement; latency=$latency" }
                    last = null
                    mutableLatency.update { latency }
                }

                else -> mutableOperations.emit(it)
            }
        }
    }

    private suspend fun Transport.readOperations(block: suspend (Operation) -> Unit) {
        while (!isClosed) {
            val operation = readOperation(resources.parser)
            try {
                block(operation)
            } catch (ex: Throwable) {
                log.warn(ex) { "Encountered an exception while handling operation:" }
            }
        }

        mutableState.update { ConnectionState.Disconnected }
        println("socket closed")
    }
}