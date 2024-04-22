package nats.jetstream.client

public interface ObjectsClient {
    /**
     * The [JetStreamClient] instance used by this [ObjectsClient] instance.
     */
    public val client: JetStreamClient


}