package dimensional.knats.connection

import dimensional.kyuso.task.Task
import io.ktor.network.sockets.*

public sealed class NatsConnectionState {
    internal interface Active {
        val conn: Connection
    }

    public data object Disconnected : NatsConnectionState()

    public data class Connected(override val conn: Connection) : Active, NatsConnectionState()

    public data class Running(override val conn: Connection, internal val readTask: Task) : Active,
        NatsConnectionState()

    public data object Detached : NatsConnectionState()
}
