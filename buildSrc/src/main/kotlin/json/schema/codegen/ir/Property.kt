package json.schema.codegen.ir

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock

data class Property(
    val name: String,
    val type: Type,
    val defaultValue: CodeBlock? = null,
    override val annotations: List<AnnotationSpec> = emptyList(),
    override val docs: CodeBlock = EmptyCodeBlock,
) : IR, Documented, Annotated