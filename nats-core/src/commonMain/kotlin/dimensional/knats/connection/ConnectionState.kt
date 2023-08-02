package dimensional.knats.connection

import dimensional.knats.NatsServer
import dimensional.knats.transport.Transport
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