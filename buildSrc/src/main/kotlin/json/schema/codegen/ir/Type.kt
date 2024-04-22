package json.schema.codegen.ir

import com.squareup.kotlinpoet.TypeName

sealed interface Type : IR {
    val isNullable: Boolean

    fun withNullable(state: Boolean): Type

    @JvmInline
    value class KotlinPoet(val value: TypeName) : Type {
        override val isNullable: Boolean get() = value.isNullable

        override fun withNullable(state: Boolean): Type = KotlinPoet(value.copy(nullable = state))
    }

    /**
     * Uses a [Definition] <-> [TypeName] map to look up the final type.
     */
    data class IR(val definition: Definition, override val isNullable: Boolean) : Type {
        override fun withNullable(state: Boolean): Type = copy(isNullable = state)
    }
}
