package nats.core.protocol

import io.ktor.http.*

public class PublicationBuilder(public val subject: Subject) : BasePublicationBuilder {
    override var body: PublicationBody = PublicationBody.Empty
    override var headers: HeadersBuilder = HeadersBuilder()
    public var replyTo: Subject? = null

    /**
     * Build this publication.
     */
    public fun build(): Publication {
        val headers = if (headers.isEmpty()) null else headers.build()
        return Publication(subject, body, replyTo, headers)
    }
}