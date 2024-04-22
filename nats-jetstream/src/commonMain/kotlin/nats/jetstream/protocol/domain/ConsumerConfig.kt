package nats.jetstream.protocol.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nats.core.protocol.optional.Optional
import nats.core.protocol.optional.OptionalBoolean
import nats.core.protocol.optional.OptionalInt
import nats.core.protocol.optional.OptionalLong
import nats.core.protocol.optional.delegate.delegate
import nats.jetstream.protocol.domain.ConsumerDeliveryPolicy.*
import nats.jetstream.tools.DurationAsNanoseconds

@Serializable
public data class ConsumerConfig(
    /**
     * A unique name for a durable consumer.
     */
    val durableName: Optional<String> = Optional.Missing(),
    /**
     * A unique name for a consumer.
     */
    val name: String,
    /**
     * A short description of the purpose of this consumer.
     */
    val description: Optional<String> = Optional.Missing(),
    /**
     * The delivery policy for the consumer.
     */
    @SerialName("deliver_policy")
    val deliveryPolicy: ConsumerDeliveryPolicy,
    @SerialName("opt_start_seq")
    val optStartSeq: OptionalLong = OptionalLong.Missing,
    /**
     * A point in time in RFC3339 format including timezone, though typically in UTC.
     */
    @SerialName("opt_start_time")
    val optStartTime: Optional<Instant> = Optional.Missing(),
    @SerialName("deliver_subject")
    val deliverSubject: Optional<String> = Optional.Missing(),
    @SerialName("ack_policy")
    val ackPolicy: Optional<ConsumerAckPolicy> = Optional.Missing(), // ConsumerAckPolicy.None
    /**
     * How long to allow messages to retain un-acknowledged before attempting redelivery.
     */
    @SerialName("ack_wait")
    val ackWait: Optional<DurationAsNanoseconds> = Optional.Missing(), // 30000000000.nanoseconds
    /**
     * The number of times a message will be redelivered to consumers if not acknowledged in time.
     */
    @SerialName("max_delivery")
    val maxDelivery: OptionalLong = OptionalLong.Missing,
    @SerialName("filter_subject")
    val filterSubject: Optional<String> = Optional.Missing(),
    @SerialName("replay_policy")
    val replayPolicy: Optional<ConsumerReplayPolicy> = Optional.Missing(),
    @SerialName("sample_freq")
    val sampleFreq: Optional<String> = Optional.Missing(),
    /**
     * The rate at which messages will be delivered to clients, expressed in bits per second.
     */
    @SerialName("rate_limit_bps")
    val rateLimitBps: Optional<ULong> = Optional.Missing(),
    /**
     * The maximum number of messages without acknowledgment that can be outstanding, once this limit is reached message
     * delivery will be suspended.
     */
    @SerialName("max_ack_pending")
    val maxAckPending: OptionalLong = OptionalLong.Missing,
    /**
     * If the Consumer is idle for more than this many nanoseconds an empty message with Status header 100 will be sent
     * indicating the consumer is still alive.
     */
    @SerialName("idle_heartbeat")
    val idleHeartbeat: Optional<DurationAsNanoseconds> = Optional.Missing(),
    /**
     * For push consumers this will regularly send an empty mess with Status header 100 and a reply subject, consumers
     * must reply to these messages to control the rate of message delivery.
     */
    @SerialName("flow_control")
    val flowControl: OptionalBoolean = OptionalBoolean.Missing,
    /**
     * The number of pulls that can be outstanding on a pull consumer, pulls received after this is reached are ignored.
     */
    @SerialName("max_waiting")
    val maxWaiting: OptionalLong = OptionalLong.Missing,
    /**
     * Creates a special consumer that does not touch the Raft layers, not for general use by clients, internal use only.
     */
    val direct: OptionalBoolean = OptionalBoolean.Missing,
    /**
     * Delivers only the headers of messages in the stream and not the bodies. Additionally, adds Nats-Msg-Size header
     * to indicate the size of the received payload.
     */
    @SerialName("headers_only")
    val headersOnly: OptionalBoolean = OptionalBoolean.Missing,
    /**
     * The largest batch property that may be specified when doing a pull on a Pull Consumer.
     */
    @SerialName("max_batch")
    val maxBatch: OptionalInt = OptionalInt.Missing,
    /**
     * The maximum value that may be set when doing a pull on a Pull Consumer.
     */
    @SerialName("max_expires")
    val maxExpires: Optional<DurationAsNanoseconds> = Optional.Missing(),
    /**
     * The maximum bytes value that maybe set when doing a pull on a Pull Consumer.
     */
    @SerialName("max_bytes")
    val maxBytes: OptionalLong = OptionalLong.Missing,
    /**
     * Duration that instructs the server to clean up ephemeral consumers that are inactive for that long.
     */
    @SerialName("inactive_threshold")
    val inactiveThreshold: Optional<DurationAsNanoseconds> = Optional.Missing(),
    /**
     * List of durations in Go format that represents a retry timescale for NaK'd messages.
     */
    val backoff: Optional<List<DurationAsNanoseconds>> = Optional.Missing(),
    /**
     * When set do not inherit the replica count from the stream but specifically set it to this amount.
     */
    @SerialName("num_replicas")
    val numReplicas: OptionalLong = OptionalLong.Missing,
    /**
     * Force the consumer state to be kept in memory rather than inherit the setting from the stream.
     */
    @SerialName("max_storage")
    val memStorage: OptionalBoolean = OptionalBoolean.Missing
) {
    public sealed class DeliveryPolicy(public val value: ConsumerDeliveryPolicy) {
        public data object All : DeliveryPolicy(ConsumerDeliveryPolicy.All)

        public data object Last : DeliveryPolicy(ConsumerDeliveryPolicy.Last)

        public data object New : DeliveryPolicy(ConsumerDeliveryPolicy.New)

        public data class ByStartSeq(public val startSeq: Long) : DeliveryPolicy(ByStartSequence)

        public data class ByStartTime(public val startTime: Instant) : DeliveryPolicy(ByStartTime)

        public data object LastPerSubject : DeliveryPolicy(ConsumerDeliveryPolicy.LastPerSubject)

    }

    public class Builder(
        public var name: String,
        public var policy: DeliveryPolicy,
    ) {
        private var _durableName: Optional<String> = Optional.Missing()
        public var durableName: String? by ::_durableName.delegate()

        private var _description: Optional<String> = Optional.Missing()
        public var description: String? by ::_description.delegate()

        private var _deliverSubject: Optional<String> = Optional.Missing()
        public var deliverSubject: String? by ::_deliverSubject.delegate()

        private var _ackPolicy: Optional<ConsumerAckPolicy> = Optional.Missing()
        public var ackPolicy: ConsumerAckPolicy? by ::_ackPolicy.delegate()

        private var _ackWait: Optional<DurationAsNanoseconds> = Optional.Missing()
        public var ackWait: DurationAsNanoseconds? by ::_ackWait.delegate()

        private var _maxDelivery: OptionalLong = OptionalLong.Missing
        public var maxDelivery: Long? by ::_maxDelivery.delegate()

        private var _filterSubject: Optional<String> = Optional.Missing()
        public var filterSubject: String? by ::_filterSubject.delegate()

        private var _replayPolicy: Optional<ConsumerReplayPolicy> = Optional.Missing()
        public var replayPolicy: ConsumerReplayPolicy? by ::_replayPolicy.delegate()

        private var _sampleFreq: Optional<String> = Optional.Missing()
        public var sampleFreq: String? by ::_sampleFreq.delegate()

        private var _rateLimitBps: Optional<ULong> = Optional.Missing()
        public var rateLimitBps: ULong? by ::_rateLimitBps.delegate()

        private var _maxAckPending: OptionalLong = OptionalLong.Missing
        public var maxAckPending: Long? by ::_maxAckPending.delegate()

        private var _idleHeartbeat: Optional<DurationAsNanoseconds> = Optional.Missing()
        public var idleHeartbeat: DurationAsNanoseconds? by ::_idleHeartbeat.delegate()

        private var _flowControl: OptionalBoolean = OptionalBoolean.Missing
        public var flowControl: Boolean? by ::_flowControl.delegate()

        private var _maxWaiting: OptionalLong = OptionalLong.Missing
        public var maxWaiting: Long? by ::_maxWaiting.delegate()

        private var _direct: OptionalBoolean = OptionalBoolean.Missing
        public var direct: Boolean? by ::_direct.delegate()

        private var _headersOnly: OptionalBoolean = OptionalBoolean.Missing
        public var headersOnly: Boolean? by ::_headersOnly.delegate()

        private var _maxBatch: OptionalInt = OptionalInt.Missing
        public var maxBatch: Int? by ::_maxBatch.delegate()

        private var _maxExpires: Optional<DurationAsNanoseconds> = Optional.Missing()
        public var maxExpires: DurationAsNanoseconds? by ::_maxExpires.delegate()

        private var _maxBytes: OptionalLong = OptionalLong.Missing
        public var maxBytes: Long? by ::_maxBytes.delegate()

        private var _inactiveThreshold: Optional<DurationAsNanoseconds> = Optional.Missing()
        public var inactiveThreshold: DurationAsNanoseconds? by ::_inactiveThreshold.delegate()

        private var _backoff: Optional<List<DurationAsNanoseconds>> = Optional.Missing()
        public var backoff: List<DurationAsNanoseconds>? by ::_backoff.delegate()

        private var _numReplicas: OptionalLong = OptionalLong.Missing
        public var numReplicas: Long? by ::_numReplicas.delegate()

        private var _memStorage: OptionalBoolean = OptionalBoolean.Missing
        public var memStorage: Boolean? by ::_memStorage.delegate()

        public fun build(): ConsumerConfig = ConsumerConfig(
            durableName = _durableName,
            name = name,
            description = _description,
            deliverSubject = _deliverSubject,
            ackPolicy = _ackPolicy,
            ackWait = _ackWait,
            maxDelivery = _maxDelivery,
            filterSubject = _filterSubject,
            replayPolicy = _replayPolicy,
            sampleFreq = _sampleFreq,
            rateLimitBps = _rateLimitBps,
            maxAckPending = _maxAckPending,
            idleHeartbeat = _idleHeartbeat,
            flowControl = _flowControl,
            maxWaiting = _maxWaiting,
            direct = _direct,
            headersOnly = _headersOnly,
            maxBatch = _maxBatch,
            maxExpires = _maxExpires,
            maxBytes = _maxBytes,
            inactiveThreshold = _inactiveThreshold,
            backoff = _backoff,
            numReplicas = _numReplicas,
            memStorage = _memStorage,
            optStartTime = when (val p = policy) {
                is DeliveryPolicy.ByStartTime -> Optional.Value(p.startTime)
                else -> Optional.Missing()
            },
            optStartSeq = when (val p = policy) {
                is DeliveryPolicy.ByStartSeq -> OptionalLong.Value(p.startSeq)
                else -> OptionalLong.Missing
            },
            deliveryPolicy = policy.value
        )
    }
}
