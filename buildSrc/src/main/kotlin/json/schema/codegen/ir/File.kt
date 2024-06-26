package json.schema.codegen.ir

import com.squareup.kotlinpoet.CodeBlock

data class File(
    val name: String,
    val definitions: List<Definition> = emptyList(),
    val packageName: String,
    override val docs: CodeBlock = EmptyCodeBlock,
) : IR, Documented
