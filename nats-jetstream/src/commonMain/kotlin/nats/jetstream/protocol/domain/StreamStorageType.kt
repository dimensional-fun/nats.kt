package nats.jetstream.protocol.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The storage backend used by a Stream.
 */
@Serializable
public enum class StreamStorageType {
    @SerialName("file")
    File,

    @SerialName("memory")
    Memory
}