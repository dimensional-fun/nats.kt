package nats.jetstream.client

import nats.jetstream.protocol.domain.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration

@Suppress("MemberVisibilityCanBePrivate")
public data class BucketConfiguration(
    /**
     * The maximum age for a value in this bucket.
     */
    val ttl: Duration = Duration.ZERO,
    /**
     * The maximum number of bytes a single value can store.
     */
    val maxValueSize: Int = -1,
    /**
     * The maximum number of bytes this bucket can store.
     */
    val maxBucketSize: Long = -1,
    /**
     * The number of replicas a message must be stored on.
     */
    val replicaCount: Int = 1,
    /**
     * The storage type for this bucket.
     */
    val storageType: StreamStorageType = StreamStorageType.File,
    /**
     * The placement of replicas for this bucket.
     */
    val placement: StreamReplicaPlacement? = null,
    /**
     * The mirror configuration for this bucket.
     */
    val mirror: StreamMirror? = null,
    /**
     * The republish configuration for this bucket.
     */
    val republish: StreamRepublishConfiguration? = null,
    /**
     * The sources for this bucket.
     */
    val sources: List<StreamMirror> = emptyList(),

) {
    /**
     * Apply this configuration to a stream builder.
     */
    public fun applyTo(builder: StreamConfig.Builder)    {
        builder.allowRollupHeaders = true
        builder.allowDirect = true
        builder.discard = StreamDiscardType.New
        builder.denyDelete = true

        when {
            mirror != null       -> builder.mirror = when {
                Bucket.hasPrefix(mirror.name) -> mirror
                !Bucket.hasPrefix(mirror.name) -> mirror.copy(name = Bucket.prefix(mirror.name))
                else -> mirror
            }

            sources.isNotEmpty() -> for (source in sources) builder.source(when {
                Bucket.hasPrefix(source.name) -> source
                !Bucket.hasPrefix(source.name) -> source.copy(name = Bucket.prefix(source.name))
                else -> source
            })

            else -> builder.subjects = listOf(Bucket.subject(builder.name) + ">")
        }

        builder.maxAge = ttl
        builder.storage = storageType
        builder.maxBytes = maxBucketSize
        builder.republish = republish
        builder.placement = placement
        builder.numReplicas = replicaCount
        builder.maxMessageSize = maxValueSize
    }

    public class Builder {
        public var ttl: Duration = Duration.ZERO

        public var maxValueSize: Int = -1

        public var maxBucketSize: Long = -1

        public var replicaCount: Int = 1

        public var storageType: StreamStorageType = StreamStorageType.File

        public var placement: StreamReplicaPlacement? = null

        public var mirror: StreamMirror? = null

        public var republish: StreamRepublishConfiguration? = null

        public var sources: MutableList<StreamMirror> = mutableListOf()

        public fun source(source: StreamMirror) {
            sources.add(source)
        }

        public fun build(): BucketConfiguration = BucketConfiguration(ttl, maxValueSize, maxBucketSize, replicaCount, storageType, placement, mirror, republish, sources)
    }
}


@OptIn(ExperimentalContracts::class)
public inline fun BucketConfiguration.Builder.mirror(
    name: String,
    block: StreamMirror.Builder.() -> Unit,
) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    mirror = StreamMirror.Builder(name).apply(block).build()
}

@OptIn(ExperimentalContracts::class)
public inline fun BucketConfiguration.Builder.republish(
    src: String,
    dest: String,
    block: StreamRepublishConfiguration.Builder.() -> Unit
) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    republish = StreamRepublishConfiguration.Builder(src, dest).apply(block).build()
}

/**
 *
 */
@OptIn(ExperimentalContracts::class)
public inline fun BucketConfiguration.Builder.source(
    name: String,
    block: StreamMirror.Builder.() -> Unit,
) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    source(StreamMirror.Builder(name).apply(block).build())
}

