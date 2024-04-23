package nats.jetstream.protocol

import io.ktor.http.*
import nats.core.protocol.BasePublicationBuilder
import nats.core.protocol.PublicationBody
import nats.core.protocol.Subject

public data class StreamPublication(
    /**
     * The subject of the publication.
     */
    val subject: Subject,
    /**
     * The headers of the publication.
     */
    val headers: Headers,
    /**
     * The body of the publication.
     */
    val body: PublicationBody,
    /**
     *
     */
    val options: Options
) {
    public data class Options(
        val messageId: String? = null,
        val expectedStream: String? = null,
        val expectedLastSequence: Long? = null,
        val expectedLastSubjectSequence: Long? = null,
        val expectedLastMessageId: String? = null
    ) {
        @Suppress("MemberVisibilityCanBePrivate")
        public class Builder {
            public var messageId: String? = null
            public var expectedStream: String? = null
            public var expectedLastSequence: Long? = null
            public var expectedLastSubjectSequence: Long? = null
            public var expectedLastMessageId: String? = null

            public fun build(): Options = Options(
                messageId = messageId,
                expectedStream = expectedStream,
                expectedLastSequence = expectedLastSequence,
                expectedLastSubjectSequence = expectedLastSubjectSequence,
                expectedLastMessageId = expectedLastMessageId
            )
        }
    }

    public class Builder(public var subject: Subject) : BasePublicationBuilder {
        override var body: PublicationBody = PublicationBody.Empty
        override var headers: HeadersBuilder = HeadersBuilder()

        private var _optionsBuilder: Options.Builder.() -> Unit = {}

        public fun options(block: Options.Builder.() -> Unit) {
            _optionsBuilder = block
        }

        public fun build(): StreamPublication {
            val headers = headers.build()
            val options = Options.Builder().apply(_optionsBuilder).build()
            return StreamPublication(subject, headers, body, options)
        }
    }
}
