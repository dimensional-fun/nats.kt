package nats.core.protocol

import nats.core.tools.CRLF
import nats.core.tools.SPACE
import nats.core.tools.ktor.readFully
import nats.core.tools.ktor.readUntilDelimitersTo
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import naibu.common.pool.use
import naibu.io.SmallMemoryPool
import naibu.io.slice.Slice
import naibu.io.slice.contentEquals
import naibu.io.slice.get
import naibu.text.charset.Charset
import naibu.text.charset.Charsets
import naibu.text.charset.decodeIntoString
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal fun Byte.estimateOpLength(): Int? = when (toInt().toChar().uppercaseChar()) {
    'M', '+' -> 3
    'I', 'H', 'P', '-' -> 4
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

internal suspend fun ByteWriteChannel.writeAsText(value: Any) = writeStringUtf8(value.toString())

internal suspend fun ByteWriteChannel.writeASCII(value: String) = writeTextNaibu(value, charset = Charsets.US_ASCII)

internal suspend fun ByteWriteChannel.writeSubject(value: Subject) = writeTextNaibu(value.value, charset = Charsets.US_ASCII)

internal suspend fun <T> ByteWriteChannel.writeArgument(value: T?, block: suspend ByteWriteChannel.(T) -> Unit) {
    if (value == null) return
    writeByte(SPACE)
    block(value)
}

internal fun Output.writeCRLF() = writeFully(CRLF)

internal suspend fun ByteWriteChannel.writeCRLF() = writeFully(CRLF)

internal suspend fun ByteWriteChannel.writeTextNaibu(value: CharSequence, charset: Charset) {
    val bytes = charset.encode(value.toString(), value.indices)
    writeFully(bytes)
}
