package nats.jetstream.protocol

import kotlinx.serialization.Serializable
import nats.core.protocol.optional.Optional
import nats.core.protocol.optional.OptionalInt
import nats.core.protocol.optional.delegate.delegate

@Serializable
public data class StreamsRequest(
    /**
     * Limit the list to streams matching this subject filter.
     */
    val subject: Optional<String> = Optional.Missing(),
    val offset: OptionalInt = OptionalInt.Missing,
) {
    public class Builder {
        private var _subject: Optional<String> = Optional.Missing()
        public var subject: String? by ::_subject.delegate()

        private var _offset: OptionalInt = OptionalInt.Missing
        public var offset: Int? by ::_offset.delegate()

        public fun build(): StreamsRequest = StreamsRequest(subject = _subject, offset = _offset)
    }
}
