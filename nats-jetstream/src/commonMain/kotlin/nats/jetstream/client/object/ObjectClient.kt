package nats.jetstream.client.`object`

import nats.jetstream.client.JetStreamClient

public interface ObjectClient {
    /**
     * The [JetStreamClient] instance used by this [ObjectClient] instance.
     */
    public val client: JetStreamClient


}