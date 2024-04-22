package nats.jetstream.client

import nats.jetstream.api.StreamsApi

public interface Streams {
    public val api: StreamsApi
    public val js: JetStream

    /**
     * Get a [Stream] instance for the given [stream name][name] regardless of whether it actually exists.
     *
     * @param name The name of the Stream
     */
    public operator fun get(name: String): Stream

    /**
     * Create a new Stream.
     *
     * @param name     The name of the Stream to add.
     * @param subjects A list of subjects for the Stream to consume from.
     */
    public suspend fun add(name: String, vararg subjects: String): Stream
}