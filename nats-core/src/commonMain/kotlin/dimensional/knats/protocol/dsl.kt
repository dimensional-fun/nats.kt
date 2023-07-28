package dimensional.knats.protocol

import dimensional.knats.tools.CR
import dimensional.knats.tools.LF
import dimensional.knats.tools.SPACE
import dimensional.knats.tools.ktor.readFully
import dimensional.knats.tools.ktor.readUntilDelimitersTo
import dimensional.knats.tools.ktor.writeTextNaibu
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import naibu.common.pool.use
import naibu.ext.asInt
import naibu.io.SmallMemoryPool
import naibu.text.charset.Charsets
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal fun Byte.estimateOpLength(): Int? = when (toInt().toChar().uppercaseChar()) {
    'M', '+' -> 3
    'I', 'H', 'P', '-' -> 4
    'C' -> 7
    else -> null
}

internal suspend fun ByteReadChannel.ensureCRLF() = SmallMemoryPool.use {
    readFully(it, 0, 2)

    val a = it.load(0)
    val b = it.load(1)
    require(a == CR && b == LF) {
        "Didn't read CRLF, got ${a.asInt().toChar()}, ${b.asInt().toChar()}"
    }
}

@OptIn(ExperimentalContracts::class)
internal suspend inline fun <T> ByteReadChannel.ensureCRLF(block: (ByteReadChannel) -> T): T {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return try {
        block(this)
    } finally {
        ensureCRLF()
    }
}


public suspend fun ByteReadChannel.readUntilCRLF(): ByteReadPacket = buildPacket {
    readUntilDelimitersTo(CR, LF, this)
}

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
