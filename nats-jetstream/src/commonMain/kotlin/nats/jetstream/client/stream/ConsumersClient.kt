package nats.jetstream.client.stream

import nats.jetstream.api.JetStreamApiException
import nats.jetstream.api.catchNotFound
import nats.jetstream.protocol.ConsumerCreateRequest
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

@JvmInline
public value class ConsumersClient(public val stream: StreamBehavior) {
    /**
     * Get a [ConsumerBehavior] instance for the given [consumer name][name] regardless of whether it actually exists.
     *
     * @param name The name of the Consumer
     * @return The [ConsumerBehavior] instance.
     */
    public operator fun get(name: String): ConsumerBehavior = ConsumerBehavior(stream.client, name, stream.name)

    /**
     * Fetch all consumers for this stream.
     *
     * @param offset the offset to start at.
     * @return the list of consumers.
     * @throws JetStreamApiException If an error occurs while fetching the list of consumers.
     */
    public suspend fun list(offset: Int = 0): List<Consumer> =
        stream.client.api.consumers.list(stream.name, offset).consumers.map { Consumer(stream.client, it.name, stream.name, it) }

    /**
     * Get the names of all consumers for this stream.
     *
     * @param offset the offset to start at.
     * @throws JetStreamApiException If an error occurs while fetching the list of consumer names.
     */
    public suspend fun names(offset: Int = 0): List<String> =
        stream.client.api.consumers.names(stream.name, offset).consumers

    /**
     * Fetches the [Consumer] with the given name, or `null` if it does not exist.
     *
     * @param name The name of the consumer to fetch.
     * @throws JetStreamApiException If an error occurs while fetching the consumer.
     */
    public suspend fun info(name: String): Consumer {
        val (info) = stream.client.api.consumers.info(stream.name, name)
        return Consumer(stream.client, name, stream.name, info)
    }

    /**
     * Fetches the [Consumer] with the given name, or `null` if it does not exist.
     *
     * @param name The name of the consumer to fetch.
     * @throws Exception If an error occurs while fetching the consumer.
     */
    public suspend fun infoOrNull(name: String): Consumer? = catchNotFound { info(name) }

    /**
     * Create a new consumer for this stream.
     *
     * @param name  The name of the consumer to create.
     * @param block A lambda that configures the consumer.
     * @return The created [Consumer].
     * @throws JetStreamApiException If an error occurs while creating the consumer.
     */
    @OptIn(ExperimentalContracts::class)
    public suspend inline fun create(
        name: String,
        block: ConsumerCreateRequest.Builder.() -> Unit = {},
    ): Consumer {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

        val resp = stream.client.api.consumers.create(
            ConsumerCreateRequest.Builder(stream.name, name)
                .apply(block)
                .build()
        )

        return Consumer(stream.client, name, stream.name, resp.info)
    }
}
