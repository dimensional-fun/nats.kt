package nats.jetstream.client.stream

import nats.jetstream.api.JetStreamApiException
import nats.jetstream.api.StreamsApi
import nats.jetstream.api.catchNotFound
import nats.jetstream.client.JetStreamClient
import nats.jetstream.protocol.StreamCreateRequest
import nats.jetstream.protocol.StreamInfoRequest
import nats.jetstream.protocol.StreamsRequest
import nats.jetstream.protocol.domain.StreamConfig
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

@JvmInline
public value class StreamsClient(public val client: JetStreamClient) {
    /**
     * The [StreamsApi] instance used by this [StreamsClient] instance.
     */
    public val api: StreamsApi get() = client.api.streams

    /**
     * Get a [StreamBehavior] instance for the given [stream name][name] regardless of whether it actually exists.
     *
     * @param name The name of the Stream
     */
    public operator fun get(name: String): StreamBehavior = StreamBehavior(client, name)
}

/**
 * Get a [StreamBehavior] instance for the given [stream name][name] regardless of whether it actually exists.
 *
 * @param name The name of the Stream
 */
@OptIn(ExperimentalContracts::class)
public suspend inline fun StreamsClient.fetch(name: String, block: StreamInfoRequest.Builder.() -> Unit = {}): Stream {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return Stream(client, name, api.info(name, StreamInfoRequest.Builder().apply(block).build()).info)
}

@OptIn(ExperimentalContracts::class)
public suspend inline fun StreamsClient.fetchOrNull(name: String, block: StreamInfoRequest.Builder.() -> Unit = {}): Stream? {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return catchNotFound { fetch(name, block) }
}

/**
 * Get the names of all Streams in the JetStream cluster.
 *
 * @param block A lambda that configures the StreamsRequest.
 * @throws JetStreamApiException If an error occurs while fetching the list of Streams.

 */
@OptIn(ExperimentalContracts::class)
public suspend fun StreamsClient.names(block: StreamsRequest.Builder.() -> Unit = {}): List<String> {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE)}
    return api.names(StreamsRequest.Builder().apply(block).build()).streams
}

/**
 *
 * @param block A lambda that configures the StreamListRequest.
 * @throws JetStreamApiException If an error occurs while fetching the list of Streams.
 */
@OptIn(ExperimentalContracts::class)
public suspend inline fun StreamsClient.list(block: StreamsRequest.Builder.() -> Unit = {}): List<Stream> {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    val response = api.list(StreamsRequest.Builder().apply(block).build())
    return response.streams.map { Stream(client, it.config.name, it) }
}

/**
 * Add a new Stream to the JetStream cluster with the given [name].
 *
 * @param name  The name of the Stream to add.
 * @param block A lambda that configures the Stream.
 * @throws JetStreamApiException If an error occurs while creating the Stream.
 */
public suspend fun StreamsClient.create(name: String, block: StreamConfig.Builder.() -> Unit): Stream {
    val config = StreamConfig.Builder(name).apply(block).build()
    val response = api.create(StreamCreateRequest(config))
    return Stream(client, name, response.info)
}
