package dimensional.knats.protocol

import io.ktor.utils.io.core.*
import kotlin.jvm.JvmInline

public interface PublicationBody {
    /**
     * The content-size of this body.
     */
    public val size: Long

    /**
     *
     */
    public fun write(output: Output)

    public data class Callback(override val size: Long, private val block: (Output) -> Unit) : PublicationBody {
        override fun write(output: Output): Unit = block(output)
    }

    /**
     *
     */
    @JvmInline
    public value class Packet(private val packet: ByteReadPacket) : PublicationBody {
        public constructor(value: ByteArray) : this(ByteReadPacket(value))

        override val size: Long get() = packet.remaining

        override fun write(output: Output) {
            output.writePacket(packet)
        }
    }

    /**
     *
     */
    public data object Empty : PublicationBody {
        override val size: Long get() = 0
        override fun write(output: Output): Unit = Unit
    }
}