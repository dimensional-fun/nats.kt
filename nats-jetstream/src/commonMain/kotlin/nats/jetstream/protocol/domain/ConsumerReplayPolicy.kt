package nats.jetstream.protocol.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class ConsumerReplayPolicy {
    @SerialName("instant")
    Instant,

    @SerialName("original")
    Original,
}