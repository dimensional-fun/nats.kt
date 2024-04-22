package nats.jetstream.api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import naibu.ext.intoOrNull
import nats.jetstream.client.JetStream
import nats.jetstream.protocol.Response
import kotlin.jvm.JvmInline

@JvmInline
public value class StreamsApi(override val js: JetStream) : JetStreamApiClient {
//    public suspend fun create(request: StreamCreateRequest): StreamCreateResponse =
//        want(request("API.STREAM.CREATE".js) { json(request) })
//
//    public suspend fun delete(): StreamDeleteResponse =
//        want(request("API.STREAM.DELETE".js))

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    private inline fun <reified T : Response> want(response: Response) = response.intoOrNull<T>()
        ?: error("Received ${response::class.serializer().descriptor.serialName} instead of ${T::class.serializer().descriptor.serialName}")
}