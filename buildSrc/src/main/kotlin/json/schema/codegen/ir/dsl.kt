package json.schema.codegen.ir

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock

interface Documented {
    val docs: CodeBlock
}

interface Annotated {
    val annotations: List<AnnotationSpec>
}

val EmptyCodeBlock = CodeBlock.builder()
    .build()