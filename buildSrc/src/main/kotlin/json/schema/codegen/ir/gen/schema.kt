package json.schema.codegen.ir.gen

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.STRING
import json.schema.JsonSchema
import json.schema.codegen.ir.IR
import json.schema.codegen.ir.Type
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

sealed interface JsonSchemaType {
    @JvmInline
    value class Single(val value: String) : JsonSchemaType

    @JvmInline
    value class Union(val values: List<String>) : JsonSchemaType
}

val JsonSchemaType.first: String
    get() = members.first()

val JsonSchemaType.members: List<String>
    get() = when (this) {
        is JsonSchemaType.Single -> listOf(value)
        is JsonSchemaType.Union -> values
    }

val JsonSchemaType.nullable: Boolean get() = "null" in this

operator fun JsonSchemaType.contains(type: String) = type in members

fun JsonSchema.getType(): JsonSchemaType? = type?.let {
    when (it) {
        is JsonPrimitive -> JsonSchemaType.Single(it.content)
        is JsonArray -> JsonSchemaType.Union(it.map { member -> member.jsonPrimitive.content })
        else -> error("Unknown type value: $it")
    }
}

fun schema(schema: JsonSchema): IR {
    val real = schema.getType()
        ?: error("Schema doesn't have type")

    val type = when {
        "string"  in real -> Type.KotlinPoet(STRING)
        "boolean" in real -> Type.KotlinPoet(BOOLEAN)
        else -> error("")
    }

    return type.withNullable(real.nullable)
}
