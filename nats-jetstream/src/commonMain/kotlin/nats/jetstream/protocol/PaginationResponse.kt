package nats.jetstream.protocol

public interface PaginationResponse : Response {
    /**
     * The total number of items in the list.
     */
    public val total: Int

    /**
     * The offset of the first item in the list.
     */
    public val offset: Int

    /**
     * The maximum number of items that can be returned.
     */
    public val limit: Int
}