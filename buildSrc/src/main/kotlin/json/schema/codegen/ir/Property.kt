package json.schema.codegen.ir

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock

data class Property(
    val name: String,
    val type: Type,
    val annotations: List<AnnotationSpec> = emptyList(),
    val defaultValue: CodeBlock? = null,
    override val docs: CodeBlock = EmptyCodeBlock,
) : Documented