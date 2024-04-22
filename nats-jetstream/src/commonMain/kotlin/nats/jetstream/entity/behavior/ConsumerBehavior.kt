package nats.jetstream.entity.behavior

import nats.jetstream.api.JetStreamApiException
import nats.jetstream.client.JetStreamClient
import nats.jetstream.entity.Consumer
import nats.jetstream.entity.JetStreamEntity

public interface ConsumerBehavior : JetStreamEntity {
    /**
     * The name of this consumer, unique within the stream.
     */
    public val name: String

    /**
     * The name of the stream this consumer belongs to.
     */
    public val streamName: String

    /**
     * The [StreamBehavior] for the stream this consumer belongs to.
     */
    public val stream: StreamBehavior
        get() = StreamBehavior(client, streamName)

    /**
     * Delete this consumer.
     *
     * @return Whether the request was successful.
     * @throws JetStreamApiException if an error occurs while deleting the consumer.
     */
    public suspend fun delete(): Boolean = client.api.consumers.delete(name).success

    /**
     *
     */
    public suspend fun resolve(): Consumer = fetch()

    /**
     *
     */
    public suspend fun resolveOrNull(): Consumer? = fetchOrNull()

    /**
     *
     */
    public suspend fun fetch(): Consumer = stream.consumers.info(name)

    /**
     *
     */
    public suspend fun fetchOrNull(): Consumer? = stream.consumers.infoOrNull(name)
}

public fun ConsumerBehavior(client: JetStreamClient, stream: String, name: String): ConsumerBehavior =
    object : ConsumerBehavior {
        override val name: String get() = name
        override val streamName: String get() = stream
        override val client: JetStreamClient get() = client

        override fun equals(other: Any?): Boolean = when(other) {
            is ConsumerBehavior -> other.streamName == streamName && name == other.name
            else -> false
        }

        override fun hashCode(): Int = arrayOf(streamName, name).contentHashCode()

        override fun toString(): String = "ConsumerBehavior(streamName=$streamName, name=$name, client=$client)"
    }
