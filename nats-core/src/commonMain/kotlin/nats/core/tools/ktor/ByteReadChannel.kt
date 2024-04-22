package nats.core.tools.ktor

import nats.core.tools.size
import io.ktor.utils.io.*
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import naibu.common.pool.use
import naibu.ext.ktor.io.ktor
import naibu.ext.ktor.io.naibu
import naibu.io.SmallMemoryPool
import naibu.io.memory.Memory
import naibu.io.memory.get
import naibu.io.memory.set
import naibu.io.slice.get
import naibu.math.toIntSafe
import io.ktor.utils.io.bits.Memory as KtorMemory

public suspend fun ByteReadChannel.readUntilDelimiter(delimiter: Byte): ByteReadPacket = buildPacket {
    readUntilDelimitersTo(byteArrayOf(delimiter), this)
}

/**
 * Copies bytes into [out] until either provided delimiters occur.
 */
public suspend fun ByteReadChannel.readUntilDelimitersTo(delimiters: ByteArray, out: Output): Int {
    var done = false
    var copiedTotal = 0
    while (!done && !isClosedForRead) {
        read { memory, start, end ->
            var i = 0
            for (value in memory.naibu()[start.toIntSafe()..<end.toIntSafe()]) {
                if (value in delimiters) {
                    done = true
                    break
                }

                i++
                out.writeByte(value)
            }

            copiedTotal += i
            i
        }
    }

    return copiedTotal
}

internal suspend fun ByteReadChannel.discardValues(values: ByteArray): Long = SmallMemoryPool.use { peeked ->
    var copiedTotal = 0L
    var done = false
    while (!done && !isClosedForRead) {
        awaitContent()

        val copied = peekTo(peeked, 0, peeked.size32)
        if (copied < 1) continue

        var i = 0L
        while (i < peeked.size) {
            if (peeked[i] in values) {
                i++
                continue
            }

            done = true
            break
        }

        copiedTotal += i
    }

    discardExact(copiedTotal)
    return copiedTotal
}

internal suspend fun ByteReadChannel.readA(size: Int = 1, block: (source: KtorMemory, range: LongRange) -> Int): Int =
    read(size) { source, start, end -> block(source, start..<end) }

internal fun KtorMemory.copyTo(dst: Memory, dstOffset: Long, range: LongRange) =
    copyTo(dst.ktor(), range.first, range.size, dstOffset)

internal suspend fun ByteReadChannel.peekTo(dst: Memory, dstOffset: Long, length: Int): Long {
    var copying = length.toLong()
    readA(length) { s, r ->
        copying = copying.coerceAtMost(r.size)
        s.copyTo(dst, dstOffset, r.first..<copying)
        0
    }

    return copying
}

internal suspend fun ByteReadChannel.tryPeek(offset: Long = 0): Byte = SmallMemoryPool.use {
    readA { source, range ->
        it[0] = source[range.first + offset]
        0
    }

    it.load(0)
}

internal suspend fun ByteReadChannel.readFully(dst: Memory, offset: Long, length: Int) {
    var read = 0
    while (read < length) readA { source, range ->
        val copying = (length - read).coerceAtMost(range.size.toInt())
        source.copyTo(dst.ktor(), range.first, copying.toLong(), offset + read)
        read += copying
        copying
    }
}
