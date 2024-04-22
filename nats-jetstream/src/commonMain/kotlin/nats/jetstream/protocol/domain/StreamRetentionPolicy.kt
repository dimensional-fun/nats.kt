package nats.jetstream.protocol.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Specifies how many messages are retained in a stream, once exceeded old messages are removed.
 */
@Serializable
public enum class StreamRetentionPolicy {
    @SerialName("limits")
    Limits,

    @SerialName("interest")
    Interest,

    @SerialName("workqueue")
    WorkQueue
}