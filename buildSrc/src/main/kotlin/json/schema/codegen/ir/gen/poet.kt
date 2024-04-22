package json.schema.codegen.ir.gen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import json.schema.codegen.ir.*

//data class Context(
//    val definitions: MutableMap<Definition, Generated>,
//    val file: File,
//) {
//    fun def(value: Definition): TypeName? = definitions.computeIfAbsent(value) {
//        it.toTypeSpec(this)
//    }
//}
//
//fun Property.toPropertySpec(context: Context): PropertySpec = PropertySpec.builder(name, type)
//    .build()
//
//fun Definition.toTypeSpec(context: Context): TypeSpec = when (this) {
//    is Definition.Class -> TypeSpec.classBuilder(name)
//        .addKdoc(docs)
//        .addAnnotations(annotations)
//        .build()
//
//    is Definition.Enum -> TypeSpec.enumBuilder(name)
//        .addKdoc(docs)
//        .addAnnotations(annotations)
//        .build()
//
//    is Definition.KotlinPoet -> type
//}