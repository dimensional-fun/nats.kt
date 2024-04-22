package nats.core.connection

import nats.core.NatsServer
import nats.core.transport.Transport
import kotlinx.coroutines.Job

public sealed class ConnectionState {
    public data object Disconnected : ConnectionState()

    public data class Connected(
        val ts: Transport,
        val server: NatsServer,
        internal val reader: Job,
    ) : ConnectionState()

    public data object Detached : ConnectionState()
}