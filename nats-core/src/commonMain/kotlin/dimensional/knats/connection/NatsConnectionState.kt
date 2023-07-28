package dimensional.knats.connection

import dimensional.knats.connection.transport.Transport
import dimensional.kyuso.task.Task

public sealed class NatsConnectionState {
    public data object Disconnected : NatsConnectionState()

    public data class Connected(val ts: Transport, val reader: Task) : NatsConnectionState()

    public data object Detached : NatsConnectionState()
}
