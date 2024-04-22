package json.schema.codegen.ir

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeSpec

sealed interface Definition : IR, Documented {
    val name: String?

    /**
     *
     */
    data class Class(
        override val name: String,
        override val docs: CodeBlock = EmptyCodeBlock,
        val constructors: Constructors = Constructors(),
        val properties: List<Property> = emptyList()
    ) : Definition {
        data class Constructors(
            val primary: Function? = null,
            val secondary: List<Function>? = null,
        )
    }

    /**
     *
     */
    data class Enum(override val name: String, override val docs: CodeBlock = EmptyCodeBlock) : Definition

    /**
     *
     */
    data class KotlinPoet(val type: TypeSpec) : Definition {
        override val docs: CodeBlock get() = type.kdoc

        override val name: String? get() = type.name
    }
}
