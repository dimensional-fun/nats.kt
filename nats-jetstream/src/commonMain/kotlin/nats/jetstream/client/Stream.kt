package nats.jetstream.client

public interface Stream {
    /**
     *
     */
    public val js: JetStream

    /**
     * The name of this stream.
     */
    public val name: String
}