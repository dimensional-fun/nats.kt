package nats.core.protocol.optional.delegate

import nats.core.protocol.optional.OptionalLong
import nats.core.protocol.optional.optional
import nats.core.protocol.optional.value
import kotlin.jvm.JvmName
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

public fun KMutableProperty0<OptionalLong>.delegate(): ReadWriteProperty<Any?, Long?> =
    object : ReadWriteProperty<Any?, Long?> {

        override fun getValue(thisRef: Any?, property: KProperty<*>): Long? {
            return this@delegate.get().value
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long?) {
            val optional = if (value == null) OptionalLong.Missing
            else OptionalLong.Value(value)
            this@delegate.set(optional)
        }

    }

@JvmName("provideNullableDelegate")
public fun KMutableProperty0<OptionalLong?>.delegate(): ReadWriteProperty<Any?, Long?> =
    object : ReadWriteProperty<Any?, Long?> {

        override fun getValue(thisRef: Any?, property: KProperty<*>): Long? {
            return this@delegate.get().value
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long?) {
            this@delegate.set(value?.optional())
        }

    }
