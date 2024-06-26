package nats.jetstream.client.stream

import nats.jetstream.client.JetStreamClient
import nats.jetstream.protocol.domain.StreamInfo

public class Stream(
    override val client: JetStreamClient,
    override val name: String,
    public val info: StreamInfo
) : StreamBehavior {
    /**
     * The description of this stream.
     */
    public val description: String? get() = info.config.description

    override suspend fun resolve(): Stream = this

    override suspend fun resolveOrNull(): Stream = this

    override fun equals(other: Any?): Boolean = when (other)  {
        is StreamBehavior -> other.name == name
        else -> false
    }

    override fun hashCode(): Int = arrayOf(name).contentHashCode()

    override fun toString(): String = "Stream(name=$name, data=$info, client=$client"
}
