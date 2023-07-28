package dimensional.knats.tools.ktor

import dimensional.knats.tools.CRLF
import io.ktor.utils.io.core.*
import naibu.text.charset.Charset

internal fun Output.writeCRLF() = writeFully(CRLF)

internal fun Output.writeTextNaibu(value: CharSequence, charset: Charset) {
    val bytes = charset.encode(value.toString(), value.indices)
    writeFully(bytes)
}

