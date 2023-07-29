package dimensional.knats.internal.connection

import dimensional.knats.internal.transport.Transport
import dimensional.kyuso.task.Task

public sealed class NatsConnectionState {
    public data object Disconnected : NatsConnectionState()

    public data class Connected(val ts: Transport, internal val reader: Task) : NatsConnectionState()

    public data object Detached : NatsConnectionState()
}