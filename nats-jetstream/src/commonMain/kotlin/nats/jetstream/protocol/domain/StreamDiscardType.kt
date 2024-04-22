package nats.jetstream.protocol.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The behavior to take when a Stream has reached its limits.
 */
@Serializable
public enum class StreamDiscardType {
    /**
     * Discard old messages.
     */
    @SerialName("old")
    Old,

    /**
     * Discard new messages.
     */
    @SerialName("new")
    New
}