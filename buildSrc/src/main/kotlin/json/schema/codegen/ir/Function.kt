package json.schema.codegen.ir

import com.squareup.kotlinpoet.CodeBlock

data class Function(
    val name: String?,
    val receiver: Definition? = null,
    val parameters: List<Parameter> = emptyList(),
    override val docs: CodeBlock = EmptyCodeBlock,
) : IR, Documented