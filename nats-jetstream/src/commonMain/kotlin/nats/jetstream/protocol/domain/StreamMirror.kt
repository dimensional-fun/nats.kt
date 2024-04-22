package nats.jetstream.protocol.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nats.core.protocol.optional.Optional
import nats.core.protocol.optional.OptionalLong
import nats.core.protocol.optional.delegate.delegate

@Serializable
public data class StreamMirror(
    /**
     * Stream name
     */
    public val name: String,
    /**
     * Sequence to start replicating from
     */
    @SerialName("opt_start_seq")
    public val optStartSeq: OptionalLong = OptionalLong.Missing,
    /**
     * Time stamp to start replicating from
     */
    @SerialName("opt_start_time")
    public val optStartTime: Optional<String> = Optional.Missing(),
    /**
     * Replicate only a subset of messages based on filter
     */
    @SerialName("filter_subject")
    public val filterSubject: Optional<String> = Optional.Missing(),
    /**
     * Configuration referencing a stream source in another account or JetStream domain
     */
    public val external: Optional<StreamMirrorExternal> = Optional.Missing(),
) {
    public class Builder(public var name: String) {
        private var _optStartSeq: OptionalLong = OptionalLong.Missing
        public var optStartSeq: Long? by ::_optStartSeq.delegate()

        private var _optStartTime: Optional<String> = Optional.Missing()
        public var optStartTime: String? by ::_optStartTime.delegate()

        private var _filterSubject: Optional<String> = Optional.Missing()
        public var filterSubject: String? by ::_filterSubject.delegate()

        private var _external: Optional<StreamMirrorExternal> = Optional.Missing()
        public var external: StreamMirrorExternal? by ::_external.delegate()

        public fun build(): StreamMirror = StreamMirror(name, _optStartSeq, _optStartTime, _filterSubject, _external)
    }
}