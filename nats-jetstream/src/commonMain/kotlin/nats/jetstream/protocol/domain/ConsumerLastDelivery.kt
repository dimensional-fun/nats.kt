package nats.jetstream.protocol.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ConsumerLastDelivery(
    /**
     * The sequence number of the Consumer.
     */
    @SerialName("consumer_seq")
    val consumerSeq: ULong,
    /**
     * The sequence number of the Stream.
     */
    @SerialName("stream_seq")
    val streamSeq: ULong,
    /**
     * The last time a message was delivered or acknowledged (for ack_floor).
     */
    @SerialName("last_active")
    val lastActive: Instant? = null
)
