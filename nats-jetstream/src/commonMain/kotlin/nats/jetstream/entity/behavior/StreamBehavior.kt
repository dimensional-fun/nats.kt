package nats.jetstream.entity.behavior

import nats.jetstream.api.catchNotFound
import nats.jetstream.api.JetStreamApiException
import nats.jetstream.client.ConsumersClient
import nats.jetstream.client.JetStreamClient
import nats.jetstream.client.MessagesClient
import nats.jetstream.client.fetch
import nats.jetstream.entity.Stream

public interface StreamBehavior {
    /**
     *
     */
    public val client: JetStreamClient

    /**
     * The name of this stream.
     */
    public val name: String

    /**
     * The consumers manager for this [StreamBehavior] instance.
     */
    public val consumers: ConsumersClient get() = ConsumersClient(this)

    public val messages: MessagesClient get() = MessagesClient(this)

    /**
     * Get the [Stream] instance for this behavior, or fetch if this is not a state-ful instance.
     *
     * @throws JetStreamApiException If an error occurs while fetching the stream information.
     */
    public suspend fun resolve(): Stream = fetch()

    /**
     * Get the [Stream] instance for this behavior, or fetch if this is not a stateful instance.
     * Returns `null` if the stream does not exist.
     *
     * @throws JetStreamApiException if the stream exists but there was an error fetching its information.
     */
    public suspend fun resolveOrNull(): Stream? = fetchOrNull()

    /**
     * Fetch the stream information.
     *
     * @throws JetStreamApiException If an error occurs while deleting the stream.
     */
    public suspend fun fetch(): Stream = client.streams.fetch(name)

    /**
     * Fetch the stream information, or `null` if the stream does not exist.
     *
     * @throws JetStreamApiException if the stream exists but there was an error fetching its information.
     */
    public suspend fun fetchOrNull(): Stream? = catchNotFound { fetch() }

    /**
     * Delete this stream.
     *
     * @return `true` if the stream was deleted, or `false` <wip>.
     * @throws JetStreamApiException if an error occurs while deleting the stream.
     */
    public suspend fun delete(): Boolean = client.streams.api.delete(name).success

    /**
     * Delete this stream, or return `null` if the stream does not exist.
     */
    // I'm not sure what the boolean return value is supposed to signify here ?
    public suspend fun remove(): Boolean? = catchNotFound { delete() }
}

public inline fun StreamBehavior(
    js: JetStreamClient,
    name: String,
): StreamBehavior = object : StreamBehavior {
    override val client: JetStreamClient get() = js
    override val name: String get() = name

    override fun equals(other: Any?): Boolean = when (other) {
        is StreamBehavior -> name == other.name
        else -> false
    }

    override fun hashCode(): Int = arrayOf(name).contentHashCode()

    override fun toString(): String = "StreamBehavior(name=$name, client=$client)"
}
