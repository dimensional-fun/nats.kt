package nats.jetstream.client

import dimensional.knats.client.Client

public interface JetStream {
    /**
     * The [Client] instance used by this [JetStream] instance.
     */
    public val client: Client

    /**
     * The streams manager for this [JetStream Client][JetStream].
     */
    public val streams: Streams
}