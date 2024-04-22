package nats.jetstream.protocol.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class ConsumerAckPolicy {
    @SerialName("none")
    None,

    @SerialName("all")
    All,

    @SerialName("explicit")
    Explicit
}