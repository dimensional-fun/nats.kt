package dimensional.knats.tools.ktor

import dimensional.knats.tools.size
import io.ktor.utils.io.*
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import naibu.common.pool.use
import naibu.ext.ktor.io.ktor
import naibu.io.SmallMemoryPool
import naibu.io.memory.Memory
import naibu.io.memory.get
import naibu.math.bit.asInt

/**
 * Copies bytes into [out] until either provided delimiters occur.
 */
public suspend fun ByteReadChannel.readUntilDelimitersTo(a: Byte, b: Byte, out: Output): Int {
    var done = false
    var copiedTotal = 0
    while (!done && !isClosedForRead) {
        read { memory, start, end ->
            var i = 0
            while (i + start < end) when (val value = memory[i + start]) {
                a, b -> {
                    done = true
                    break
                }

                else -> {
                    i++
                    out.writeByte(value)
                }
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
    outer@ while (!done && !isClosedForRead) {
        awaitContent()

        val copied = peekTo(peeked, 0, 0, peeked.size)
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

internal suspend fun ByteReadChannel.peekTo(dst: Memory, dstOffset: Long, offset: Long, length: Long): Long {
    var copying = length
    read { source, start, endExclusive ->
        copying = length.coerceAtMost((start..<endExclusive).size.toLong())
        source.copyTo(dst.ktor(), start + offset, copying, dstOffset)
        0
    }

    return copying
}

internal suspend fun ByteReadChannel.tryPeek(offset: Long = 0): Int {
    if (availableForRead < 1) {
        return -1
    }

    return SmallMemoryPool.use {
        read { source, start, _ ->
            source.copyTo(it.ktor(), start + offset, 1, 0)
            0
        }

        it.load(0).asInt()
    }
}

internal suspend fun ByteReadChannel.readFully(dst: Memory, offset: Long, length: Int) {
    var read = 0
    while (read < length) read { source, start, end ->
        val copying = (length - read).coerceAtMost((start..<end).size)
        source.copyTo(dst.ktor(), start, copying.toLong(), offset + read)
        read += copying
        copying
    }
}
