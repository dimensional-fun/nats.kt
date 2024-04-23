package nats.jetstream.protocol

import kotlinx.serialization.Serializable

@Serializable
public data class StreamPublishResponse(
    /**
     * The server assigned sequence number for the published message.
     */
    val seq: Long,
    /**
     * The name of the stream that was published to.
     */
    val stream: String,
    /**
     * The domain name.
     */
    val domain: String? = null,
    /**
     * Whether the server detected the published message was a duplicate.
     */
    val duplicate: Boolean = false,
)
