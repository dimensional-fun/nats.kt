package nats.jetstream.protocol.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nats.core.protocol.optional.OptionalBoolean
import nats.core.protocol.optional.delegate.delegate

@Serializable
public data class StreamRepublishConfiguration(
    /**
     * The source subject to republish
     */
    public val src: String,
    /**
     *StreamCreateReq The destination to publish to
     */
    public val dest: String,
    /**
     * Only send message headers, no bodies
     */
    @SerialName("headers_only")
    public val headersOnly: OptionalBoolean = OptionalBoolean.Missing,
) {
    @Suppress("MemberVisibilityCanBePrivate")
    public class Builder(public var src: String, public var dest: String) {
        private var _headersOnly: OptionalBoolean = OptionalBoolean.Missing
        public var headersOnly: Boolean? by ::_headersOnly.delegate()

        public fun build(): StreamRepublishConfiguration = StreamRepublishConfiguration(src, dest, _headersOnly)
    }
}