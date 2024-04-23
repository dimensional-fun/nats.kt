package nats.core.protocol

import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.StringFormat
import naibu.text.charset.Charset
import naibu.text.charset.Charsets
import naibu.text.charset.decodeIntoString

public sealed interface Delivery : HasPayload {
    /** The subject that this message was sent with. */
    public val subject: Subject

    /** The ID of the subscription which received this message. */
    public val sid: String

    /** The subject that a reply should be sent to. */
    public val replyTo: Subject?

    /** The [Headers] that were sent along with this message. */
    public val headers: Headers?

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