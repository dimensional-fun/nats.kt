package json.schema.codegen.ir

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeSpec

sealed interface Definition : IR, Documented, Annotated {
    val name: String?

    /**
     *
     */
    data class Class(
        override val name: String,
        override val docs: CodeBlock = EmptyCodeBlock,
        override val annotations: List<AnnotationSpec> = emptyList(),
        val constructors: Constructors = Constructors(),
        val properties: List<Property> = emptyList(),
    ) : Definition {
        data class Constructors(
            val primary: Function? = null,
            val secondary: List<Function>? = null,
        )
    }

    /**
     *
     */
    data class Enum(
        override val name: String,
        override val docs: CodeBlock = EmptyCodeBlock,
        override val annotations: List<AnnotationSpec> = emptyList(),
    ) : Definition

    /**
     *
     */
    @JvmInline
    value class KotlinPoet(val type: TypeSpec) : Definition {
        override val docs: CodeBlock get() = type.kdoc

        override val annotations: List<AnnotationSpec> get() = type.annotations

        override val name: String? get() = type.name
    }
}
