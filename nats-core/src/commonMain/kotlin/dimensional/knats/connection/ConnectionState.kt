package dimensional.knats.connection

import dimensional.knats.transport.Transport
import dimensional.kyuso.task.Task

public sealed class ConnectionState {
    public data object Disconnected : ConnectionState()

    public data class Connected(val ts: Transport, internal val reader: Task) : ConnectionState()

    public data object Detached : ConnectionState()
}