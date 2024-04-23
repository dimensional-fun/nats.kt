package nats.jetstream.client.stream

import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import naibu.serialization.DefaultFormats
import nats.core.client.request
import nats.core.protocol.Delivery.Companion.charsetHint
import nats.core.protocol.Subject
import nats.core.protocol.read
import nats.core.tools.Json
import nats.jetstream.api.JetStreamApiException
import nats.jetstream.api.catchNotFound
import nats.jetstream.protocol.Error
import nats.jetstream.protocol.StreamMessageGetRequest
import nats.jetstream.protocol.StreamPublication
import nats.jetstream.protocol.StreamPublishResponse
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline
import kotlin.time.Duration.Companion.seconds

@JvmInline
public value class MessagesClient(public val stream: StreamBehavior) {
    /**
     * Get a message from the stream.
     *
     * @param request The request to get the message.
     * @return The message info, or `null` if the message does not exist.
     * @throws JetStreamApiException If the JetStream API returns an error.
     */
    public suspend fun get(request: StreamMessageGetRequest): MessageInfo? = catchNotFound { fetch(request) }

    /**
     * Fetch a message from the stream.
     *
     * **Notes:**
     * - If the message does not exist, this method will throw an exception.
     * - This method will resolve the current stream to determine whether to use the Direct API or the Stream API.
     *
     * @param request The request to get the message.
     * @throws JetStreamApiException If the JetStream API returns an error.
     */
    public suspend fun fetch(request: StreamMessageGetRequest): MessageInfo {
        val direct = stream.resolve().info.config.allowDirect
        return if (direct) {
            stream.client.api.direct.get(stream.name, request)
        } else {
            val (message) = stream.client.api.streams.getMessage(stream.name, request)
            MessageInfo.Stream(stream.name, message)
        }
    }

    /**
     * Publish a message to the stream.
     *
     * @param publication The publication to publish.
     * @return The response from the JetStream API.
     * @throws JetStreamApiException If the JetStream API returns an error.
     */
    public suspend fun publish(publication: StreamPublication): StreamPublishResponse {
        val response = withTimeout(1.seconds)  {
            stream.client.core.request(publication.subject) {
                header("Nats-Msg-Id", publication.options.messageId)
                header("Nats-Expected-Stream", publication.options.expectedStream)
                header("Nats-Expected-Last-Msg-Id", publication.options.expectedLastMessageId)
                header("Nats-Expected-Last-Sequence", publication.options.expectedLastSequence)
                header("Nats-Expected-Last-Subject-Sequence", publication.options.expectedLastSubjectSequence)

                body = publication.body
                headers.appendAll(publication.headers)
            }
        }

        val resp = response.read<JsonObject>(
            DefaultFormats.Json,
            response.charsetHint
        )

        if ("error" in resp) {
            val error = DefaultFormats.Json.decodeFromJsonElement<Error>(resp["error"]!!)
            throw JetStreamApiException(error)
        }

        return DefaultFormats.Json.decodeFromJsonElement(resp)
    }
}

/**
 *
 */
@OptIn(ExperimentalContracts::class)
public suspend inline fun MessagesClient.get(block: StreamMessageGetRequest.Builder.() -> Unit): MessageInfo? {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return get(StreamMessageGetRequest.Builder().apply(block).build())
}

/**
 *
 */
@OptIn(ExperimentalContracts::class)
public suspend inline fun MessagesClient.fetch(block: StreamMessageGetRequest.Builder.() -> Unit): MessageInfo {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return fetch(StreamMessageGetRequest.Builder().apply(block).build())
}

/**
 * Publish a message to the stream.
 *
 * @param subject The subject to publish the message to.
 * @param block   The block to build the publication.
 */
public suspend inline fun MessagesClient.publish(
    subject: Subject,
    block: StreamPublication.Builder.() -> Unit,
): StreamPublishResponse {
    val publication = StreamPublication.Builder(subject)
        .apply(block)
        .build()

    return publish(publication)
}
