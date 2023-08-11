package nats.jetstream.protocol.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class StreamRepublishConfiguration(
    /**
     * The source subject to republish
     */
    public val src: String,
    /**
     * The destination to publish to
     */
    public val dest: String,
    /**
     * Only send message headers, no bodies
     */
    @SerialName("headers_only")
    public val headersOnly: Boolean? = false,
)