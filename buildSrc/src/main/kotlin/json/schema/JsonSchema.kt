package json.schema

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable
data class JsonSchema(
    @SerialName("\$id")
    val id: String = "",
    @SerialName("\$schema")
    val schema: String = "",
    @SerialName("\$ref")
    val ref: String = "",
    @SerialName("\$comment")
    val comment: String = "",
    val title: String = "",
    val description: String = "",
    val default: JsonElement? = null,
    val readOnly: Boolean = false,
    val writeOnly: Boolean = false,
    val examples: List<JsonElement> = emptyList(),
    val multipleOf: Int? = null,
//    val maximum: Double? = null,
//    val exclusiveMaximum: Double? = null,
//    val minimum: Double? = null,
//    val exclusiveMinimum: Double? = null,
//    val maxLength: Int? = null,
//    val minLength: Int? = null,
    val pattern: String? = null,
    val additionalItems: List<JsonSchema> = emptyList(),
    val items: Items? = null,
    val maxItems: Int? = null,
    val minItems: Int? = null,
    val uniqueItems: Boolean = false,
    val contains: OrBoolean<JsonSchema>? = null,
    val maxProperties: Int? = null,
    val required: List<String> = emptyList(),
    val additionalProperties: OrBoolean<JsonSchema>? = null,
    val definitions: Map<String, JsonSchema> = emptyMap(),
    val properties: Map<String, JsonSchema> = emptyMap(),
    val patternProperties: Map<String, JsonSchema> = emptyMap(),
    //TODO:
    val dependencies: JsonElement? = null,
    // TODO:
    val propertyNames: JsonSchema? = null,
    val const: JsonElement? = null,
    val enum: List<JsonElement> = emptyList(),
    // TODO:
    val type: JsonElement? = null,
    val format: String? = null,
    val contentMediaType: String? = null,
    val contentEncoding: String? = null,
    @SerialName("if")
    val ifSomething: JsonSchema? = null,
    val then: JsonSchema? = null,
    @SerialName("else")
    val elseSomething: JsonSchema? = null,
    val allOf: List<JsonSchema> = emptyList(),
    val anyOf: List<JsonSchema> = emptyList(),
    val oneOf: List<JsonSchema> = emptyList(),
    val not: OrBoolean<JsonSchema>? = null,
) {
    @Serializable(with = Items.Serializer::class)
    sealed interface Items {
        @JvmInline
        @Serializable
        value class Single(val value: OrBoolean<JsonSchema>) : Items

        @JvmInline
        @Serializable
        value class Multiple(val value: List<OrBoolean<JsonSchema>>) : Items

        companion object Serializer : JsonContentPolymorphicSerializer<Items>(Items::class) {
            override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Items> = when (element) {
                is JsonArray -> Multiple.serializer()
                else -> Single.serializer()
            }
        }
    }

    @Serializable(with = OrBoolean.Serializer::class)
    sealed interface OrBoolean<out T> {
        @JvmInline
        @Serializable
        value class Bool(val value: Boolean) : OrBoolean<Nothing>

        @JvmInline
        @Serializable
        value class Value<T>(val value: T) : OrBoolean<T>

        class Serializer<T>(private val inner: KSerializer<T>) : KSerializer<OrBoolean<T>> {
            override val descriptor: SerialDescriptor = inner.descriptor

            override fun deserialize(decoder: Decoder): OrBoolean<T> {
                val dec = decoder as JsonDecoder

                /**/
                val elm = dec.decodeJsonElement()
                if (elm is JsonPrimitive) {
                    val bool = elm.booleanOrNull
                    if (bool != null) {
                        return Bool(bool)
                    }
                }

                return Value(dec.json.decodeFromJsonElement(inner, elm))
            }

            override fun serialize(encoder: Encoder, value: OrBoolean<T>) = when (value) {
                is Bool -> encoder.encodeBoolean(value.value)
                is Value -> encoder.encodeSerializableValue(inner, value.value)
            }
        }
    }
}
