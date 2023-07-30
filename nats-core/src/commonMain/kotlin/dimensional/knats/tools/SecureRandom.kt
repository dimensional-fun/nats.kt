package dimensional.knats.tools

import naibu.io.slice.asSlice
import naibu.io.slice.readInt
import kotlin.random.Random
import org.kotlincrypto.SecureRandom as CryptSecureRandom

public object SecureRandom : Random() {
    private val inner = CryptSecureRandom()

    override fun nextInt(): Int = inner.nextBytesOf(4)
        .asSlice()
        .readInt()

    override fun nextBits(bitCount: Int): Int =
        nextInt() and (1 shl bitCount).dec()

    override fun nextBytes(array: ByteArray, fromIndex: Int, toIndex: Int): ByteArray =
        inner.nextBytesOf(toIndex - fromIndex)
}