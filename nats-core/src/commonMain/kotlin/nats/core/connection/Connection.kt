package nats.core.connection

import nats.core.annotations.InternalNatsApi
import nats.core.protocol.Operation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

@InternalNatsApi
public interface Connection {
    public val scope: CoroutineScope

    /**
     * The current state of the connection.
     */
    public val state: StateFlow<ConnectionState>

    /**
     * The calculated RTT for PING/PONGs.
     */
    public val latency: StateFlow<Duration?>

    /**
     * The operations received by this connection.
     */
    public val operations: SharedFlow<Operation>

    /**
     * Connect to the remote NATS server.
     */
    public suspend fun connect()

    /**
     * Disconnect from the remote NATS server.
     */
    public suspend fun disconnect()

    /**
     * Detach this connection, disconnecting & preventing further connectivity.
     */
    public suspend fun detach()

    /**
     * Send an operation to the remote NATS server, this method will throw an exception
     * if the [current state][state] is not [ConnectionState.Connected].
     *
     * @param operation The operations to send.
     */
    public suspend fun send(operation: Operation, vararg operations: Operation)
}