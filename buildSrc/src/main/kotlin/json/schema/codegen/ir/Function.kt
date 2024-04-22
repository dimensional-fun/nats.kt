package json.schema.codegen.ir

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock

data class Function(
    val name: String?,
    val receiver: Definition? = null,
    val parameters: List<Parameter> = emptyList(),
    override val docs: CodeBlock = EmptyCodeBlock,
    override val annotations: List<AnnotationSpec> = emptyList(),
) : IR, Documented, Annotated