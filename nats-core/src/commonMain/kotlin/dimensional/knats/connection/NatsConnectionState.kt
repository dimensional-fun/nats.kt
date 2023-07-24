package dimensional.knats.connection

import dimensional.knats.connection.transport.Transport
import dimensional.kyuso.task.Task

public sealed class NatsConnectionState {
    internal interface Active {
        val ts: Transport
    }

    public data object Disconnected : NatsConnectionState()

    public data class Connected(override val ts: Transport) : Active, NatsConnectionState()

    public data class Running(override val ts: Transport, internal val readTask: Task) : Active,
        NatsConnectionState()

    public data object Detached : NatsConnectionState()
}
