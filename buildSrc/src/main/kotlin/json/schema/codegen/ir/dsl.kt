package json.schema.codegen.ir

import com.squareup.kotlinpoet.CodeBlock

interface Documented {
    val docs: CodeBlock
}

val EmptyCodeBlock = CodeBlock.builder()
    .build()