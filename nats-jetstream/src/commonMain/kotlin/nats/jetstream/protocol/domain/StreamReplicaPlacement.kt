package nats.jetstream.protocol.domain

import kotlinx.serialization.Serializable
import nats.core.protocol.optional.Optional

@Serializable
public data class StreamReplicaPlacement(
    /**
     * The desired cluster name to place the stream
     */
    public val cluster: Optional<String> = Optional.Missing(),
    public val tags: Optional<List<String>> = Optional.Missing(),
)