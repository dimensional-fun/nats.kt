package nats.jetstream.api

import dimensional.knats.protocol.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import naibu.ext.intoOrNull
import nats.jetstream.api.JetStreamApiClient.Companion.js
import nats.jetstream.api.JetStreamApiClient.Companion.request
import nats.jetstream.client.JetStream
import nats.jetstream.protocol.Response
import nats.jetstream.protocol.StreamCreateRequest
import nats.jetstream.protocol.StreamCreateResponse
import nats.jetstream.protocol.StreamDeleteResponse
import kotlin.jvm.JvmInline

@JvmInline
public value class StreamsApi(override val js: JetStream) : JetStreamApiClient {
    public suspend fun create(request: StreamCreateRequest): StreamCreateResponse =
        want(request("API.STREAM.CREATE".js) { json(request) })

    public suspend fun delete(): StreamDeleteResponse =
        want(request("API.STREAM.DELETE".js))

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    private inline fun <reified T : Response> want(response: Response) = response.intoOrNull<T>()
        ?: error("Received ${response::class.serializer().descriptor.serialName} instead of ${T::class.serializer().descriptor.serialName}")
}