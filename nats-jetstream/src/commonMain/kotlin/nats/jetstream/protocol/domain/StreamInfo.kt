package nats.jetstream.protocol.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import nats.core.protocol.optional.Optional

@Serializable
public data class StreamInfo(
    /**
     * The active configuration for the Stream.
     */
    val config: StreamConfig,
    /**
     * Detail about the current State of the Stream.
     */
    val state: StreamState,
    /**
     * Timestamp when the Stream was created.
     */
    val created: Instant,
    val cluster: Optional<StreamCluster> = Optional.Missing(),
    /**
     * Information about an upstream stream source in a mirror.
     */
    val mirror: Optional<StreamSource> = Optional.Missing(),
    /**
     * Streams being sourced into this Stream.
     */
    val sources: Optional<List<StreamSource>> = Optional.Missing(),
    /**
     * List of alternative locations to read mirrored data, sorted by priority.
     */
    val alternates: Optional<List<StreamAlternativeMirrorLocation>> = Optional.Missing()
)
