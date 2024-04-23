package nats.jetstream.client.kv

import kotlinx.datetime.Instant
import nats.core.annotations.InternalNatsApi
import nats.core.protocol.optional.orEmpty
import nats.jetstream.protocol.domain.StreamInfo
import nats.jetstream.protocol.domain.StreamState

public data class KeyValueBucketInfo(
    /**
     * The bucket configuration.
     */
    val config: KeyValueBucketConfiguration,
    /**
     * The state of the underlying stream.
     */
    val state: StreamState,
    /**
     * The time when the bucket was created.
     */
    val created: Instant,
    /**
     * The internal stream information.
     */
    @property:InternalNatsApi val inner: StreamInfo
) {
    public constructor(inner: StreamInfo) : this(
        KeyValueBucketConfiguration(
            name = inner.config.name.removePrefix("KV_"),
            ttl = inner.config.maxAge,
            maxValueSize = inner.config.maxMessageSize,
            maxBucketSize = inner.config.maxBytes,
            replicaCount = inner.config.numReplicas,
            storageType = inner.config.storage,
            placement = inner.config.placement.value,
            mirror = inner.config.mirror.value,
            republish = inner.config.republish.value,
            sources = inner.config.sources.orEmpty(),
        ),
        inner.state,
        inner.created,
        inner
    )
}