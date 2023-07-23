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
     * The payload of this message.
     * Note: depending on the implementation this may just be a copy of the original payload.
     */
    public val payload: ByteReadPacket?
}