package nats.jetstream.protocol.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class ConsumerDeliveryPolicy {
    @SerialName("all")
    All,

    @SerialName("last")
    Last,

    @SerialName("new")
    New,

    @SerialName("by_start_sequence")
    ByStartSequence,

    @SerialName("by_start_time")
    ByStartTime,

    @SerialName("last_per_subject")
    LastPerSubject;
}
