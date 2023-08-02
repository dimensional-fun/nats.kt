package codegen

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

val SERIALIZABLE = Serializable::class
val SERIAL_NAME = SerialName::class

fun serialName(name: String): AnnotationSpec = AnnotationSpec.builder(SERIAL_NAME)
    .addMember("%S", name)
    .build()

fun TypeSpec.Builder.serializable() = addAnnotation(SERIALIZABLE)

fun TypeSpec.Builder.addSerialName(name: String): TypeSpec.Builder = addAnnotation(serialName(name))

fun PropertySpec.Builder.addSerialName(name: String): PropertySpec.Builder = addAnnotation(serialName(name))
