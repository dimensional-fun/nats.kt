package dimensional.knats.protocol

import dimensional.knats.tools.CRLF
import dimensional.knats.tools.SPACE
import dimensional.knats.tools.ktor.readFully
import dimensional.knats.tools.ktor.readUntilDelimitersTo
import dimensional.knats.tools.ktor.writeTextNaibu
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import naibu.common.pool.use
import naibu.io.SmallMemoryPool
import naibu.io.slice.Slice
import naibu.io.slice.contentEquals
import naibu.io.slice.get
import naibu.text.charset.Charsets
import naibu.text.charset.decodeIntoString
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal fun Byte.estimateOpLength(): Int? = when (toInt().toChar().uppercaseChar()) {
    'M', '+' -> 3
    'I', 'H', 'P', '-' -> 4
    'C' -> 7
    else -> null
}

internal suspend fun ByteReadChannel.ensureCRLF() = SmallMemoryPool.use { crlf ->
    readFully(crlf, 0, 2)

    require(crlf[0..1] eq CRLF) {
        "Didn't read CRLF, got ${crlf[0..1].decodeIntoString()}"
    }
}

@OptIn(ExperimentalContracts::class)
internal suspend inline fun <T> ByteReadChannel.ensureCRLF(block: (ByteReadChannel) -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return try {
        block(this)
    } finally {
        ensureCRLF()
    }
}

public suspend fun ByteReadChannel.readUntilCRLF(): ByteReadPacket = buildPacket {
    readUntilDelimitersTo(CRLF, this)
}

internal inline infix fun Slice.eq(other: ByteArray) = contentEquals(other)

internal fun Output.writeAsText(value: Any) = writeText(value.toString())

internal fun Output.writeSubject(value: String) = writeTextNaibu(value, charset = Charsets.US_ASCII)

internal fun <T> Output.writeArgument(value: T?, block: Output.(T) -> Unit) {
    if (value == null) return
    writeByte(SPACE)
    block(value)
}
