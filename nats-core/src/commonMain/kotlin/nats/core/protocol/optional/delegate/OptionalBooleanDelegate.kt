package nats.core.protocol.optional.delegate

import nats.core.protocol.optional.OptionalBoolean
import nats.core.protocol.optional.optional
import nats.core.protocol.optional.value
import kotlin.jvm.JvmName
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

public fun KMutableProperty0<OptionalBoolean>.delegate(): ReadWriteProperty<Any?, Boolean?> =
    object : ReadWriteProperty<Any?, Boolean?> {

        override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean? {
            return this@delegate.get().value
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean?) {
            val optional = if (value == null) OptionalBoolean.Missing
            else OptionalBoolean.Value(value)
            this@delegate.set(optional)
        }

    }

@JvmName("provideNullableDelegate")
public fun <T> KMutableProperty0<OptionalBoolean?>.delegate(): ReadWriteProperty<T, Boolean?> =
    object : ReadWriteProperty<T, Boolean?> {

        override fun getValue(thisRef: T, property: KProperty<*>): Boolean? {
            return this@delegate.get().value
        }

        override fun setValue(thisRef: T, property: KProperty<*>, value: Boolean?) {
            this@delegate.set(value?.optional())
        }

    }
