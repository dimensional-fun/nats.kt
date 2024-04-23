package nats.jetstream.client

import nats.core.client.Client
import nats.jetstream.api.JetStreamApi
import nats.jetstream.client.kv.KeyValueClient
import nats.jetstream.client.stream.ConsumersClient
import nats.jetstream.client.stream.StreamsClient

public class JetStreamClient(
    /**
     * The [Client] instance used by this [JetStreamClient] instance.
     */
    public val core: Client
) {

    /**
     * The [JetStreamApi] instance used by this [JetStreamClient] instance.
     */
    public val api: JetStreamApi get() = JetStreamApi(core)

    //

    /**
     * The key-value store manager for this [JetStream Client][JetStreamClient].
     */
    public val kv: KeyValueClient get() = KeyValueClient(this)

    /**
     * The streams manager for this [JetStream Client][JetStreamClient].
     */
    public val streams: StreamsClient get() = StreamsClient(this)

    /**
     * Get a [ConsumersClient] instance for the given [stream name][stream].
     *
     * @param stream The name of the Stream
     * @return The [ConsumersClient] instance.
     */
    public fun consumers(stream: String): ConsumersClient = streams[stream].consumers
}
