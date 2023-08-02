package nats.jetstream.api

import dimensional.knats.client.request
import dimensional.knats.protocol.PublicationBuilder
import io.ktor.http.*
import naibu.serialization.DefaultFormats
import naibu.serialization.deserialize
import naibu.serialization.json.Json
import nats.jetstream.client.JetStream
import nats.jetstream.protocol.ErrorResponse
import nats.jetstream.protocol.Response
import nats.jetstream.protocol.ResponseSerializer

public sealed interface JetStreamApiClient {
    public val js: JetStream

    public companion object {
        internal suspend fun JetStreamApiClient.request(
            subject: String,
            block: PublicationBuilder.() -> Unit = {},
        ): Response {
            val msg = js.client.request(subject) {
                headers[HttpHeaders.Accept] = "application/json"
                block()
            }

            val resp = msg.readText()
                ?.deserialize(ResponseSerializer, DefaultFormats.Json)
                ?: error("Couldn't deserialize JetStream response?")

            if (resp is ErrorResponse) {
                // TODO: correctly handle error responses.
                error("[${resp.error.code}/${resp.error.errCode}] ${resp.error.description}")
            }

            return resp
        }

        internal val String.js: String get() = "\$JS.$this"
    }
}
