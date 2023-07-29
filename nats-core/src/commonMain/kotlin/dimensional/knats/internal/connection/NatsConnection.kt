package dimensional.knats.internal.connection

import dimensional.knats.Connection
import dimensional.knats.annotations.InternalNatsApi
import dimensional.knats.internal.NatsResources
import dimensional.knats.internal.transport.Transport
import dimensional.knats.internal.transport.readOperation
import dimensional.knats.internal.transport.write
import dimensional.knats.protocol.Operation
import dimensional.knats.tools.child
import dimensional.kyuso.Kyuso
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import naibu.ext.intoOrNull
import naibu.logging.logging
import naibu.monads.unwrapOkOrElse
import kotlin.coroutines.coroutineContext

@OptIn(InternalNatsApi::class)
public class NatsConnection(public val resources: NatsResources) : Connection {
    public companion object {
        private val log by logging { }
    }

    /** Used for connecting to different NATS servers. */
    private val connector = NatsConnector(resources)

    override val scope: CoroutineScope = connector.scope.child(CoroutineName("Connection"))

    internal val kyuso = Kyuso(scope)

    //
    private val mutableState = MutableStateFlow<NatsConnectionState>(NatsConnectionState.Disconnected)
    private val mutableOperations = MutableSharedFlow<Operation>(extraBufferCapacity = Int.MAX_VALUE)

    override val state: StateFlow<NatsConnectionState> = mutableState.asStateFlow()
    override val operations: SharedFlow<Operation> = mutableOperations

    override suspend fun send(operation: Operation) {
        val (ts) = requireNotNull(state.value.intoOrNull<NatsConnectionState.Connected>()) {
            "This connection is not running."
        }

        ts.write(operation)
        ts.flush()
    }

    override suspend fun connect() {
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

    override suspend fun disconnect() {
        val (ts, reader) = requireNotNull(state.value.intoOrNull<NatsConnectionState.Connected>()) {
            "This NATS connection is not connected or has been detached."
        }

        /* cancel the operation reader & close the transport. */
        reader.cancel()
        ts.close()

        /*  */
        mutableState.update { NatsConnectionState.Disconnected }
    }

    override suspend fun detach() {
        try {
            disconnect()
        } finally {
            scope.cancel()
            mutableState.update { NatsConnectionState.Disconnected }
        }
    }

    private suspend fun Transport.readOperations(block: suspend (Operation) -> Unit) {
        while (coroutineContext.isActive) {
            val operation = readOperation(resources.parser)
            try {
                block(operation)
            } catch (ex: Throwable) {
                log.warn(ex) { "Encountered an exception while handling operation:" }
            }
        }
    }
}