package nats.core.protocol

import io.ktor.utils.io.*
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
    public suspend fun write(channel: ByteWriteChannel)

    /**
     *
     */
    public data class Callback(override val size: Long, private val block: suspend (ByteWriteChannel) -> Unit) : PublicationBody {
        override suspend fun write(channel: ByteWriteChannel): Unit = block(channel)
    }

    /**
     *
     */
    public data class ReadChannel(val value: ByteReadChannel, override val size: Long) : PublicationBody {
        override suspend fun write(channel: ByteWriteChannel) {
            value.copyTo(channel, size)
        }
    }

    /**
     *
     */
    @JvmInline
    public value class Packet(private val packet: ByteReadPacket) : PublicationBody {
        public constructor(value: ByteArray) : this(ByteReadPacket(value))

        override val size: Long get() = packet.remaining

        override suspend fun write(channel: ByteWriteChannel) {
            channel.writePacket(packet)
        }
    }

    /**
     *
     */
    public data object Empty : PublicationBody {
        override val size: Long get() = 0
        override suspend fun write(channel: ByteWriteChannel): Unit = Unit
    }
}