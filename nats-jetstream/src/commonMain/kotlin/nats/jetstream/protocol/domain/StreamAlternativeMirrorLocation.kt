package nats.jetstream.protocol.domain

import kotlinx.serialization.Serializable

/**
 * An alternative location to read mirrored data.
 */
@Serializable
public data class StreamAlternativeMirrorLocation(
    /**
     * The mirror stream name.
     */
    val name: String,
    /**
     * The name of the cluster holding the stream.
     */
    val cluster: String,
    /**
     * The domain holding the stream.
     */
    val domain: String? = null,
)
