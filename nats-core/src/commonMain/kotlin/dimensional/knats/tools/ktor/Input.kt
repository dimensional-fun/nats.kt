package dimensional.knats.tools.ktor

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import naibu.text.charset.Charset

internal fun Input.readUntilDelimiter(delimiter: Byte) = buildPacket {
    readUntilDelimiter(delimiter, this)
}

internal fun Input.discardValues(values: ByteArray): Long {
    var discardedTotal = 0L
    takeWhile { chunk ->
        val discarded = chunk.discardValues(values)
        discardedTotal += discarded
        discarded > 0 && !chunk.canRead()
    }

    return discardedTotal
}

internal fun Buffer.discardValues(values: ByteArray): Int {
    val start = readPosition
    val limit = writePosition
    var i = start

    while (i < limit) {
        if (memory[i] !in values) break
        i++
    }

    /**/
    val discarded = i - start
    discardExact(discarded)

    return discarded
}
