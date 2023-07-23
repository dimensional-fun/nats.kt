package dimensional.knats.protocol

import io.ktor.http.*
import io.ktor.utils.io.core.*

public interface Message {
    /**
     * The subject of this message.
     */
    public val subject: String

    /**
     * ID of the subscriber this message was sent to.
     */
    public val sid: String

    /**
     * The reply-to subject set by the publisher.
     */
    public val replyTo: String?

    /**
     * The headers associated with this message.
     */
    public val headers: Headers?

    /**
     * Get a copy of this message's [ByteReadPacket] (does not copy bytes).
     */
    public fun getPayload(): ByteReadPacket?
}