package codegen

import codegen.PropertyType.Companion.parse
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import json.schema.JsonSchema
import kotlinx.serialization.json.*

data class PropertyType(val name: String, val members: List<String>) {
    companion object {
        fun JsonElement.parse() = when (this) {
            is JsonArray -> PropertyType(
                get(0).jsonPrimitive.content,
                drop(1).map { it.jsonPrimitive.content })

            is JsonPrimitive -> PropertyType(content, emptyList())
            else -> error("unknown property type: $this")
        }
    }

    val nullable: Boolean get() = "null" in members
}

val TypeName.nullable: TypeName get() = copy(nullable = true)

fun JsonSchema.guessType(): TypeName {
    val type = type?.parse()
        ?: error("no 'type' field.")

    val real = when (type.name) {
        "string" -> STRING

        "number", "integer" -> guessIntegerType()

        "boolean" -> BOOLEAN

        "object" -> JsonObject::class.asTypeName()

        "array" -> {
//            val item = items
//                ?.guessType()
//                ?: error("Unable to guess item type of array: ${get("items")}")

            LIST.parameterizedBy(ANY)
        }

        // TODO: durations, e.g., nanoseconds
        // TODO: timestamps

        else -> error("unknown type: $type, $this")
    }

    return real.copy(nullable = type.nullable)
}

fun JsonSchema.guessIntegerType(): TypeName {
    val comment = comment.takeUnless { it.isEmpty() }
        ?.lowercase()
        ?: return INT

    val unsigned = "unsigned" in comment
    return when {
        "64" in comment -> if (unsigned) U_LONG else LONG
        "32" in comment -> if (unsigned) U_INT else INT
        else -> error("unknown integer; comment=$comment")
    }
}
