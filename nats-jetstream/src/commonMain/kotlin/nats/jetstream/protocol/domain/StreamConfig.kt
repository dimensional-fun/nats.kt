package nats.jetstream.protocol.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nats.core.protocol.optional.Optional
import nats.core.protocol.optional.delegate.delegate
import nats.core.protocol.optional.mapCopy
import nats.jetstream.tools.DurationAsNanoseconds
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration

@Serializable
public data class StreamConfig(
    /**
     * A unique name for the Stream, empty for the Stream Templates.
     */
    val name: String,
    /**
     * A short description of the purpose of this stream.
     */
    val description: String? = null,
    /**
     * A list of subjects to consume, supports wildcards. Must be empty when a mirror is configured. May be empty when
     * sources are configured.
     */
    val subjects: List<String> = emptyList(),
    /**
     * Hoe messages are retained in the Stream, once this is exceeded old messages are removed.
     */
    val retention: StreamRetentionPolicy = StreamRetentionPolicy.Limits,
    /**
     * How many consumers can be defined for a given stream. -1 for unlimited (default).
     */
    @SerialName("max_consumers")
    val maxConsumers: Long = -1,
    /**
     * How many messages may be in a Stream, oldest messages will be removed if the Stream exceeds this size. -1 for
     * unlimited (default).
     */
    @SerialName("max_msgs")
    val maxMessages: Long = -1,
    /**
     * For wildcard streams ensure that for every unique subject this many messages are kept - a per subject retention
     * limit.
     */
    @SerialName("max_msgs_per_subject")
    val maxMessagesPerSubject: Long = -1,
    /**
     * How big the stream may be, when the combined stream size exceeds this old messages are removed. -1 for unlimited (default).
     */
    @SerialName("max_bytes")
    val maxBytes: Long = -1,
    /**
     * Maximum age for any message in the stream, expressed in nanoseconds. 0 for unlimited (default).
     */
    @SerialName("max_age")
    val maxAge: DurationAsNanoseconds = Duration.ZERO,
    /**
     * The largest message that will be accepted by the Stream. -1 for unlimited (default).
     */
    @SerialName("max_msg_size")
    val maxMessageSize: Int = -1,
    /**
     * The storage backend to use for the Stream.
     */
    val storage: StreamStorageType = StreamStorageType.File,
    /**
     * How many replicas to keep for each message.
     */
    @SerialName("num_replicas")
    val numReplicas: Int = 1,
    /**
     * Disables acknowledging messages that are received by the Stream.
     */
    @SerialName("no_ack")
    val noAck: Boolean = false,
    /**
     * When the Stream is managed by a Stream Template this identifies the template that manages the Stream.
     */
    @SerialName("template_owner")
    val templateOwner: Optional<String> = Optional.Missing(),
    /**
     * When a Stream reaches its limits either old messages are deleted or new ones are denied.
     */
    val discard: StreamDiscardType = StreamDiscardType.Old,
    /**
     * The time window to track duplicate messages, 0 for default.
     */
    @SerialName("duplicate_window")
    val duplicateWindow: DurationAsNanoseconds = Duration.ZERO,
    /**
     * Placement directives to consider when placing replicas of this stream, random placement when unset.
     */
    val placement: Optional<StreamReplicaPlacement> = Optional.Missing(),
    /**
     * Maintains a 1:1 mirror of another stream with name matching this property. When a mirror is configured [subjects]
     * and [sources] must be empty.
     */
    val mirror: Optional<StreamMirror> = Optional.Missing(),
    /**
     * List of stream names to replicate into this Stream.
     */
    val sources: Optional<List<StreamMirror>> = Optional.Missing(),
    /**
     * Sealed streams do not allow messages to be deleted via limits or API, sealed streams can not be unsealed via
     * configuration update. Can only be set on already created streams via the Update API.
     */
    val sealed: Boolean = false,
    /**
     * Restricts the ability to delete messages from a stream via the API. Cannot be changed once set to true.
     */
    @SerialName("deny_delete")
    val denyDelete: Boolean = false,
    /**
     * Restricts the ability to purge messages from a stream via the API. Cannot be change once set to true.
     */
    @SerialName("deny_purge")
    val denyPurge: Boolean = false,
    /**
     * Allows the use of the Nats-Rollup header to replace all contents of a stream, or subject in a stream, with a
     * single new message.
     */
    @SerialName("allow_rollup_headers")
    val allowRollupHeaders: Boolean = false,
    /**
     * Allow higher performance, direct access to get individual messages.
     */
    @SerialName("allow_direct")
    val allowDirect: Boolean = false,
    /**
     * Allow higher performance, direct access for mirrors as well.
     */
    @SerialName("mirror_direct")
    val mirrorDirect: Boolean = false,
    /**
     * Rules for republishing messages from a stream with subject mapping onto new subjects for partitioning and more.
     */
    val republish: Optional<StreamRepublishConfiguration> = Optional.Missing(),
    /**
     * When discard policy is new and the stream is one with max messages per subject set, this will apply the new
     * behavior to every subject. Essentially turning discard new from maximum number of subjects into maximum number
     * of messages in a subject.
     */
    @SerialName("discard_new_per_subject")
    val discardNewPerSubject: Boolean = false,
) {
    @Suppress("MemberVisibilityCanBePrivate")
    public class Builder(public var name: String) {
        public var description: String? = null

        public var subjects: List<String> = emptyList()

        public var retention: StreamRetentionPolicy = StreamRetentionPolicy.Limits

        public var maxConsumers: Long = -1

        public var maxMessages: Long = -1

        public var maxMessagesPerSubject: Long = -1

        public var maxBytes: Long = -1

        public var maxAge: Duration = Duration.ZERO

        public var maxMessageSize: Int = -1

        public var storage: StreamStorageType = StreamStorageType.File

        public var numReplicas: Int = 1

        public var noAck: Boolean = false

        private var _templateOwner: Optional<String> = Optional.Missing()
        public var templateOwner: String? by ::_templateOwner.delegate()

        public var discard: StreamDiscardType = StreamDiscardType.Old

        public var duplicateWindow: Duration = Duration.ZERO

        private var _placement: Optional<StreamReplicaPlacement> = Optional.Missing()
        public var placement: StreamReplicaPlacement? by ::_placement.delegate()

        private var _mirror: Optional<StreamMirror> = Optional.Missing()
        public var mirror: StreamMirror? by ::_mirror.delegate()

        private var _sources: Optional<MutableList<StreamMirror>> = Optional.Missing()
        public var sources: MutableList<StreamMirror>? by ::_sources.delegate()

        public var sealed: Boolean = false

        public var denyDelete: Boolean = false

        public var denyPurge: Boolean = false

        public var allowRollupHeaders: Boolean = false

        public var allowDirect: Boolean = false

        public var mirrorDirect: Boolean = false

        private var _republish: Optional<StreamRepublishConfiguration> = Optional.Missing()
        public var republish: StreamRepublishConfiguration? by ::_republish.delegate()

        public var discardNewPerSubject: Boolean = false

        public fun build(): StreamConfig = StreamConfig(
            name = name,
            description = description,
            subjects = subjects,
            retention = retention,
            maxConsumers = maxConsumers,
            maxMessages = maxMessages,
            maxMessagesPerSubject = maxMessagesPerSubject,
            maxBytes = maxBytes,
            maxAge = maxAge,
            maxMessageSize = maxMessageSize,
            storage = storage,
            numReplicas = numReplicas,
            noAck = noAck,
            templateOwner = _templateOwner,
            discard = discard,
            duplicateWindow = duplicateWindow,
            placement = _placement,
            mirror = _mirror,
            sources = _sources.mapCopy(),
            sealed = sealed,
            denyDelete = denyDelete,
            denyPurge = denyPurge,
            allowRollupHeaders = allowRollupHeaders,
            allowDirect = allowDirect,
            mirrorDirect = mirrorDirect,
            republish = _republish,
            discardNewPerSubject = discardNewPerSubject
        )
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun StreamConfig.Builder.mirror(block: StreamMirror.Builder.() -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    mirror = StreamMirror.Builder(name).apply(block).build()
}

@OptIn(ExperimentalContracts::class)
public inline fun StreamConfig.Builder.republish(block: StreamRepublishConfiguration.Builder.() -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    republish = StreamRepublishConfiguration.Builder(name, name).apply(block).build()
}

@OptIn(ExperimentalContracts::class)
public inline fun StreamConfig.Builder.source(block: StreamMirror.Builder.() -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    val mirror = StreamMirror.Builder(name).apply(block).build()
    sources?.add(mirror) ?: run { sources = mutableListOf(mirror) }
}
