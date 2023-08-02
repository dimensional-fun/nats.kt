package nats.jetstream.protocol

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

public object ResponseSerializer : JsonContentPolymorphicSerializer<Response>(Response::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Response> {
        if (element !is JsonObject) {
            throw SerializationException("JetStream responses must be in the form of an object.")
        }

        return when {
            "error" in element -> ErrorResponse.serializer()
            else -> Response.serializer()
        }
    }
}