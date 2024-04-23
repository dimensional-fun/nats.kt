package nats.jetstream.api

import io.ktor.http.*
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import naibu.ext.intoOrNull
import naibu.serialization.DefaultFormats
import naibu.serialization.json.Json
import naibu.text.charset.Charsets
import nats.core.client.Client
import nats.core.client.request
import nats.core.protocol.PublicationBuilder
import nats.core.protocol.Subject
import nats.jetstream.protocol.ErrorResponse
import nats.jetstream.protocol.Response
import nats.jetstream.protocol.ResponseSerializer
import kotlin.jvm.JvmInline
import kotlin.time.Duration.Companion.seconds

@JvmInline
public value class JetStreamApi(public val core: Client) {
    public val subject: Subject get() = Subject("\$JS.API")

    public val direct: DirectApi get() = DirectApi(this)

    public val streams: StreamsApi get() = StreamsApi(this)

    public val consumers: ConsumerApi get() = ConsumerApi(this)

    /**
     * Send a request to the JetStream API.
     *
     * @param subject The subject to send the request to.
     * @param block   The block to configure the request.
     */
    internal suspend fun request(
        subject: Subject,
        block: PublicationBuilder.() -> Unit = {},
    ): Response {
        val msg = withTimeout(1.seconds) {
            core.request (subject) {
                header(HttpHeaders.Accept, "application/json")
                block()
            }
        }

        val resp = msg.read(ResponseSerializer, DefaultFormats.Json, Charsets.UTF_8)

        return if (resp is ErrorResponse) throw JetStreamApiException(resp.error) else resp
    }

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    internal inline fun <reified T : Response> want(response: Response) = response.intoOrNull<T>() ?: error(
        "Received ${response::class.serializer().descriptor.serialName} instead of ${T::class.serializer().descriptor.serialName}"
    )
}