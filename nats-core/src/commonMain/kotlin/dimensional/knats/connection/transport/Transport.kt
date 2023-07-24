package dimensional.knats.connection.transport

import io.ktor.utils.io.core.*

public interface Transport {
    /**
     * Close the connection.
     */
    public fun close()

    /**
     * Write a packet to the connection.
     */
    public suspend fun write(packet: ByteReadPacket)

    /**
     *
     */
    public suspend fun write(block: BytePacketBuilder.() -> Unit): Unit = write(buildPacket(block))

    /**
     * Read a packet from the connection, suspends until data is available.
     */
    public suspend fun read(): ByteReadPacket
}