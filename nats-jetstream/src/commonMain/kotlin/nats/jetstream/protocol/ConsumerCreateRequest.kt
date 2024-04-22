package nats.jetstream.protocol

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nats.jetstream.protocol.domain.ConsumerConfig

@Serializable
public data class ConsumerCreateRequest(
    /**
     * The name of the stream to create the consumer in.
     */
    @SerialName("stream_name")
    val streamName: String,
    val config: ConsumerConfig
) {
    public class Builder(
        public var streamName: String,
        public var consumerName: String,
    ) {
        private var consumerConfigBuilder: ConsumerConfig.Builder.() -> Unit = {}
        private var consumerPolicy: ConsumerConfig.DeliveryPolicy = ConsumerConfig.DeliveryPolicy.All

        public fun config(block: ConsumerConfig.Builder.() -> Unit) {
            val old = consumerConfigBuilder
            consumerConfigBuilder = {
                old()
                block()
            }
        }

        public fun deliverAll(): Builder {
            consumerPolicy = ConsumerConfig.DeliveryPolicy.All
            return this
        }

        public fun deliverLast(): Builder {
            consumerPolicy = ConsumerConfig.DeliveryPolicy.Last
            return this
        }

        public fun deliverNew(): Builder {
            consumerPolicy = ConsumerConfig.DeliveryPolicy.New
            return this
        }

        public fun deliverByStartSequence(value: Long): Builder {
            consumerPolicy = ConsumerConfig.DeliveryPolicy.ByStartSeq(value)
            return this
        }

        public fun deliverByStartTime(value: Instant): Builder {
            consumerPolicy = ConsumerConfig.DeliveryPolicy.ByStartTime(value)
            return this
        }

        public fun deliverLastPerSubject(): Builder {
            consumerPolicy = ConsumerConfig.DeliveryPolicy.LastPerSubject
            return this
        }

        public fun build(): ConsumerCreateRequest {
            val config = ConsumerConfig.Builder(consumerName, consumerPolicy)
                .apply(consumerConfigBuilder)
                .build()

            return ConsumerCreateRequest(streamName, config)
        }
    }
}