package nats.jetstream.protocol.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import nats.core.protocol.Subject

@Serializable
public data class StreamMessage(
    /**
     * The subject the message was originally received on.
     */
    val subject: Subject,
    /**
     * The sequence number of the message in the stream.
     */
    val seq: Long,
    /**
     * The time the message was received.
     */
    val time: Instant,
    /**
     * The base64 encoded payload of the message body.
     */
    val data: String? = null,
    /**
     * Base64 encoded headers for the message.
     */
    val hdrs: String? = null
)
