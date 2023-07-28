package dimensional.knats.connection

import dimensional.knats.connection.transport.Transport
import dimensional.knats.protocol.Operation
import dimensional.knats.tools.child
import dimensional.kyuso.Kyuso
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import naibu.ext.intoOrNull
import naibu.logging.logging
import naibu.monads.unwrapOkOrElse
import kotlin.coroutines.coroutineContext

// TODO: refactor connection state, it's pretty bad lol

public class NatsConnection(public val resources: NatsResources) {
    public companion object {
        private val log by logging { }
    }

    /** Used for connecting to different NATS servers. */
    private val connector = NatsConnector(resources)

    /**
     *
     */
    public val scope: CoroutineScope = connector.scope.child(CoroutineName("Connection"))

    //

    /** Used for scheduling tasks */
    private val kyuso = Kyuso(scope)

    //
    private val mutableState = MutableStateFlow<NatsConnectionState>(NatsConnectionState.Disconnected)
    private val mutableOperations = MutableSharedFlow<Operation>(extraBufferCapacity = Int.MAX_VALUE)

    /**
     * The current state of this NATS connection.
     */
    public val state: StateFlow<NatsConnectionState> get() = mutableState.asStateFlow()

    /**
     *
     */
    public val operations: SharedFlow<Operation> get() = mutableOperations

    /**
     *
     */
    public suspend fun send(op: Operation) {
        val (ts) = requireNotNull(state.value.intoOrNull<NatsConnectionState.Connected>()) {
            "This connection is not running."
        }

        ts.write(op)
        ts.flush()
    }

    /**
     *
     */
    public suspend fun connect() {
        val ts = connector.connect().unwrapOkOrElse { throw it }

        /* start reading operations lol. */
        val reader = kyuso.dispatch {
            ts.readOperations {
                when (it) {
                    is Operation.Ping -> send(Operation.Pong)
                    else -> mutableOperations.emit(it)
                }
            }
        }

        /* upgrade the connection. */
        mutableState.update { NatsConnectionState.Connected(ts, reader) }
    }

    /**
     *
     */
    public suspend fun disconnect() {
        val (ts, reader) = requireNotNull(state.value.intoOrNull<NatsConnectionState.Connected>()) {
            "This NATS connection is not connected or has been detached."
        }

        /* cancel the operation reader & close the transport. */
        reader.cancel()
        ts.close()

        /*  */
        mutableState.update { NatsConnectionState.Disconnected }
    }

    private suspend fun Transport.readOperations(block: suspend (Operation) -> Unit) {
        while (coroutineContext.isActive) {
            val operation = readOperation(resources.parser)
                ?: continue

            try {
                block(operation)
            } catch (ex: Throwable) {
                log.warn(ex) { "Encountered an exception while handling operation:" }
            }
        }
    }
}
