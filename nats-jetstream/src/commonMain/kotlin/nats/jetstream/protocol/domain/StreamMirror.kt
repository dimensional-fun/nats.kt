package nats.jetstream.protocol.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    public val optStartSeq: ULong? = null,
    /**
     * Time stamp to start replicating from
     */
    @SerialName("opt_start_time")
    public val optStartTime: String? = null,
    /**
     * Replicate only a subset of messages based on filter
     */
    @SerialName("filter_subject")
    public val filterSubject: String? = null,
    /**
     * Configuration referencing a stream source in another account or JetStream domain
     */
    public val external: StreamMirrorExternal? = null,
)