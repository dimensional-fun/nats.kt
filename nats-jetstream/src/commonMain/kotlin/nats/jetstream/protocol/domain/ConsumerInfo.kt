package nats.jetstream.protocol.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ConsumerInfo(
    /**
     * The Stream the consumer belongs to.
     */
    @SerialName("stream_name")
    val streamName: String,
    /**
     * A unique name for the consumer, either machine generated or the durable name.
     */
    val name: String,
    /**
     * The consumer configuration.
     */
    val config: ConsumerConfig,
    /**
     * The time the Consumer was created.
     */
    val created: Instant,
    /**
     * The last message delivered from this Consumer.
     */
    val delivered: ConsumerLastDelivery,
    /**
     * The number of messages delivered to this Consumer.
     */
    @SerialName("ack_floor")
    val ackFloor: ConsumerLastDelivery,
    /**
     * The number of messages pending for this Consumer.
     */
    @SerialName("num_ack_pending")
    val numAckPending: Long,
    /**
     * The number of messages pending for this Consumer.
     */
    @SerialName("num_redelivered")
    val numRedelivered: Long,
    /**
     * The number of pull consumers waiting for messages.
     */
    @SerialName("num_waiting")
    val numWaiting: Long,
    val cluster: StreamCluster? = null,
    /**
     * Indicates if any client is connected and receiving messages from a push consumer.
     */
    @SerialName("push_bound")
    val pushBound: Boolean? = null,
)
