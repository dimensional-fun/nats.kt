import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nats.jetstream.protocol.domain.StreamDiscardType
import nats.jetstream.protocol.domain.StreamMirrorExternal
import nats.jetstream.protocol.domain.StreamRetentionPolicy
import nats.jetstream.protocol.domain.StreamStorageType
import nats.jetstream.tools.DurationAsNanoseconds

@Serializable
@SerialName("io.nats.jetstream.api.v1.stream_create_request")
public data class StreamCreateRequest(
    /**
     * A unique name for the Stream, empty for Stream Templates.
     */
    val name: String? = null,
    /**
     * A short description of the purpose of this stream.
     */
    val description: String? = null,
    /**
     * A list of subjects to consume, supports wildcards. Must be empty when a mirror is configured. May be empty when
     * sources are configured
     */
    val subjects: List<String>? = null,
    /**
     * How messages are retained in the Stream, once this is exceeded old messages are removed.
     */
    val retention: StreamRetentionPolicy,
    /**
     * How many Consumers can be defined for a given Stream. -1 for unlimited.
     */
    @SerialName("max_consumers")
    val maxConsumers: Long,
    /**
     * How many messages may be in a Stream, oldest messages will be removed if the Stream exceeds this size. -1 for
     * unlimited.
     */
    @SerialName("max_msgs")
    val maxMessages: Long,
    /**
     * For wildcard streams ensure that for every unique subject this many messages are kept - a per subject retention limit
     */
    @SerialName("max_msgs_per_subject")
    val maxMessagesPerSubject: Long? = null,
    /**
     * How big the Stream may be, when the combined stream size exceeds this old messages are removed. -1 for unlimited.
     */
    @SerialName("max_bytes")
    val maxBytes: Long? = null,
    /**
     * Maximum age of any message in the stream, expressed in nanoseconds. 0 for unlimited.
     */
    @SerialName("max_age")
    val maxAge: DurationAsNanoseconds? = null,
    /**
     * The largest message that will be accepted by the Stream. -1 for unlimited.
     */
    @SerialName("max_msg_size")
    val maxMessageSize: Int? = null,
    /**
     * The storage backend to use for the Stream.
     */
    val storage: StreamStorageType,
    /**
     * How many replicas to keep for each message.
     */
    @SerialName("num_replicas")
    val numReplicas: Long,
    /**
     * Disables acknowledging messages that are received by the Stream.
     */
    @SerialName("no_ack")
    val noAck: Boolean? = null,
    /**
     * When the Stream is managed by a Stream Template this identifies the template that manages the Stream.
     */
    @SerialName("template_owner")
    val templateOwner: String? = null,
    /**
     *  When a Stream reach it's limits either old messages are deleted or new ones are denied
     */
    val discard: StreamDiscardType? = null,
    /**
     * The time window to track duplicate messages for, expressed in nanoseconds. 0 for default
     */
    @SerialName("duplicate_window")
    val duplicateWindow: DurationAsNanoseconds? = null,
    /**
     * Placement directives to consider when placing replicas of this stream, random placement when omitted.
     */
    val placement: Placement? = null,
    /**
     * Maintains a 1:1 mirror of another stream with name matching this property.  When a mirror is configured subjects
     * and sources must be empty.
     */
    val mirror: Source? = null,
    /**
     * List of Stream names to replicate into this Stream
     */
    val sources: List<Source>? = null,
    /**
     * Sealed streams do not allow messages to be deleted via limits or API, sealed streams can not be unsealed via
     * configuration update. Can only be set on already created streams via the Update API
     */
    val sealed: Boolean? = null,
    /**
     * Restricts the ability to delete messages from a stream via the API. Cannot be changed once set to true
     */
    @SerialName("deny_delete")
    val denyDelete: Boolean? = null,
    /**
     * Restricts the ability to purge messages from a stream via the API. Cannot be change once set to true
     */
    @SerialName("deny_purge")
    val denyPurge: Boolean? = null,
    /**
     * Allows the use of the Nats-Rollup header to replace all contents of a stream, or subject in a stream, with a
     * single new message
     */
    @SerialName("allow_rollup_hdrs")
    val allowRollupHeaders: Boolean? = null,
    /**
     * Allow higher performance, direct access to get individual messages
     */
    @SerialName("allow_direct")
    val allowDirect: Boolean? = null,
    /**
     * Allow higher performance, direct access for mirrors as well
     */
    @SerialName("mirror_direct")
    val mirrorDirect: Boolean? = null,
    /**
     * Rules for republishing messages from a stream with subject mapping onto new subjects for partitioning and more
     */
    val republish: Republish? = null,
    /**
     * When discard policy is new and the stream is one with max messages per subject set, this will apply the new
     * behavior to every subject. Essentially turning discard new from maximum number of subjects into maximum number of
     * messages in a subject.
     */
    @SerialName("discard_new_per_subject")
    val discardNewPerSubject: Boolean? = null
) {
    @Serializable
    public data class Placement(
        /**
         * The desired to place the stream.
         */
        val cluster: String,
        /**
         * Tags required on servers hosting this stream.
         */
        val tags: List<String>? = null,
    )

    @Serializable
    public data class Source(
        /**
         * Stream name
         */
        val name: String,
        /**
         * Sequence to start replicating from
         */
        @SerialName("opt_start_seq")
        val optStartSeq: ULong? = null,
        /**
         * Time stamp to start replicating from
         */
        @SerialName("opt_start_time")
        val optStartTime: Instant? = null,
        /**
         * Replicate only a subset of messages based on filter
         */
        @SerialName("filter_subject")
        val filterSubject: String? = null,
        /**
         * Configuration referencing a stream source in another account or JetStream domain
         */
        val external: StreamMirrorExternal? = null,
    )

    @Serializable
    public data class Republish(
        /**
         * The source subject to republish.
         */
        val src: String,
        /**
         * The destination to publish to.
         */
        val dest: String,
        /**
         * Only send message headers, no bodies.
         */
        @SerialName("headers_only")
        val headersOnly: Boolean? = null,
    )
}