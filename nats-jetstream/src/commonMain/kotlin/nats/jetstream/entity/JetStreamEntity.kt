package nats.jetstream.entity

import nats.jetstream.client.JetStreamClient

public interface JetStreamEntity {
    /**
     * The [JetStreamClient] instance that this entity is associated with.
     */
    public val client: JetStreamClient
}