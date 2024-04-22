package nats.jetstream.client

import nats.core.client.Client
import nats.jetstream.api.JetStreamApi

public interface JetStreamClient {
    /**
     * The [Client] instance used by this [JetStreamClient] instance.
     */
    public val core: Client

    /**
     * The [JetStreamApi] instance used by this [JetStreamClient] instance.
     */
    public val api: JetStreamApi

    //

    /**
     * The streams manager for this [JetStream Client][JetStreamClient].
     */
    public val streams: StreamsClient

    /**
     * Get a [ConsumersClient] instance for the given [stream name][stream].
     *
     * @param stream The name of the Stream
     * @return The [ConsumersClient] instance.
     */
    public fun consumers(stream: String): ConsumersClient = streams[stream].consumers
}