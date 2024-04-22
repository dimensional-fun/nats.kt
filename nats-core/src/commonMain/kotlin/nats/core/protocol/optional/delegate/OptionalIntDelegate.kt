package nats.core.protocol.optional.delegate

import nats.core.protocol.optional.OptionalInt
import nats.core.protocol.optional.optionalInt
import nats.core.protocol.optional.value
import kotlin.js.JsName
import kotlin.jvm.JvmName
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

@JsName("intDelegate")
public fun KMutableProperty0<OptionalInt>.delegate(): ReadWriteProperty<Any?, Int?> = object : ReadWriteProperty<Any?, Int?> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int? {
        return this@delegate.get().value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int?) {
        val optional = if (value == null) OptionalInt.Missing
        else OptionalInt.Value(value)
        this@delegate.set(optional)
    }

}

@JvmName("provideNullableDelegate")
public fun KMutableProperty0<OptionalInt?>.delegate(): ReadWriteProperty<Any?, Int?> = object : ReadWriteProperty<Any?, Int?> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int? {
        return this@delegate.get().value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int?) {
        this@delegate.set(value?.optionalInt())
    }

}
