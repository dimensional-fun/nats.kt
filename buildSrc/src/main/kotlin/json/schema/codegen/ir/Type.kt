package json.schema.codegen.ir

import com.squareup.kotlinpoet.TypeName

sealed class Type {
    abstract val isNullable: Boolean

    data class KotlinPoet(val value: TypeName) : Type() {
        override val isNullable: Boolean by value::isNullable
    }

    /**
     * Uses a [Definition] <-> [TypeName] map to look up the final type.
     */
    data class IR(val definition: Definition, override val isNullable: Boolean) : Type()
}
