package dimensional.knats.protocol

import io.ktor.http.*
import io.ktor.utils.io.core.*
import naibu.text.charset.Charset
import naibu.text.charset.Charsets
import naibu.text.charset.encodeIntoByteArray

public class PublicationBuilder(public val subject: String) {
    public var replyTo: String? = null
    public var body: PublicationBody = PublicationBody.Empty
    public var headers: HeadersBuilder = HeadersBuilder()

    /**
     *
     */
    public inline fun payload(
        size: Long,
        contentType: ContentType = ContentType.Application.OctetStream,
        noinline block: (Output) -> Unit,
    ) {
        body = PublicationBody.Callback(size, block)
        contentType(contentType)
    }

    /**
     *
     */
    public inline fun payload(value: ByteReadPacket, contentType: ContentType = ContentType.Application.OctetStream) {
        body = PublicationBody.Packet(value)
        contentType(contentType)
    }

    /**
     *
     */
    public inline fun payload(value: ByteArray, contentType: ContentType = ContentType.Application.OctetStream) {
        body = PublicationBody.Packet(value)
        contentType(contentType)
    }

    /**
     *
     */
    public inline fun payload(value: String, range: IntRange = value.indices, charset: Charset = Charsets.US_ASCII) {
        body = PublicationBody.Packet(value.encodeIntoByteArray(range, charset))
        contentType(ContentType.Text.Plain.withParameter("charset", charset.name))
    }

    public inline fun contentType(contentType: ContentType) {
        header(HttpHeaders.ContentType, contentType.toString(), false)
    }

    /**
     * Set a header w/ the given [name] & [value].
     */
    public inline fun header(name: String, value: String, append: Boolean = true) {
        if (append) headers.append(name, value) else headers[name] = value
    }

    /**
     * Build this publication.
     */
    public fun build(): Publication =
        if (headers.isEmpty()) {
            Publication(subject, body, replyTo)
        } else {
            Publication(subject, body, headers.build(), replyTo)
        }
}