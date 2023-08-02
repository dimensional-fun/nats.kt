package dimensional.knats.protocol

import io.ktor.http.*
import io.ktor.utils.io.core.*
import naibu.text.charset.Charset
import naibu.text.charset.Charsets
import naibu.text.charset.decodeIntoString

public sealed interface Delivery {
    /** The subject that this message was sent with. */
    public val subject: String

    /** The ID of the subscription which received this message. */
    public val sid: String

    /** The subject that a reply should be sent to. */
    public val replyTo: String?

    /** The [Headers] that were sent along with this message. */
    public val headers: Headers?

    /**
     */
    public fun getPayload(): ByteReadPacket?

    /**
     *
     */
    public fun readText(charset: Charset = charsetHint): String? =
        getPayload()?.readBytes()?.decodeIntoString(charset = charset)

    public companion object {
        public val Delivery.contentType: ContentType?
            get() = headers
                ?.get(HttpHeaders.ContentType)
                ?.let(ContentType::parse)

        public val Delivery.charsetHint: Charset
            get() = contentType?.parameter("charset")
                ?.let(Charsets::named)
                ?: Charsets.UTF_8
    }
}