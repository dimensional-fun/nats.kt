package dimensional.knats.protocol

import io.ktor.http.*

public class PublicationBuilder(public val subject: String) {
    public var replyTo: String? = null
    public var body: PublicationBody = PublicationBody.Empty
    public var headers: HeadersBuilder = HeadersBuilder()

    /**
     *
     */
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
    public fun build(): Publication {
        val headers = if (headers.isEmpty()) null else headers.build()
        return Publication(subject, body, replyTo, headers)
    }
}