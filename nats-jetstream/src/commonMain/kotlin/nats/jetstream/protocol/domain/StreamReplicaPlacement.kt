package nats.jetstream.protocol.domain

import kotlinx.serialization.Serializable

@Serializable
public data class StreamReplicaPlacement(
    /**
     * The desired cluster name to place the stream
     */
    public val cluster: String,
    public val tags: List<String>? = null,
)