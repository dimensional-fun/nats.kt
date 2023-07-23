package dimensional.knats.protocol

import dimensional.knats.tools.SPACE
import dimensional.knats.tools.writeTextNaibu
import io.ktor.utils.io.core.*
import naibu.text.charset.Charsets

internal inline infix fun ByteArray.eq(other: ByteArray) = contentEquals(other)

internal fun Output.writeAsText(value: Any) = writeText(value.toString())

internal fun Output.writeSubject(value: String) {
    writeTextNaibu(value, charset = Charsets.US_ASCII)
}

internal fun <T> Output.writeArgument(value: T?, block: Output.(T) -> Unit) {
    if (value == null) return
    writeByte(SPACE)
    block(value)
}
