package nats.core.protocol

import io.ktor.http.*

public interface BasePublicationBuilder {
    /**
     */
    public var body: PublicationBody

    /**
     */
    public var headers: HeadersBuilder

    /**
     *
     */
    public fun contentType(contentType: ContentType) {
        header(HttpHeaders.ContentType, contentType.toString(), false)
    }

    /**
     * Set a header w/ the given [name] & [value].
     */
    public fun header(name: String, value: Any?, append: Boolean = true) {
        if (value == null) return
        if (append) headers.append(name, value.toString()) else headers[name] = value.toString()
    }

    public class Basic()
}
