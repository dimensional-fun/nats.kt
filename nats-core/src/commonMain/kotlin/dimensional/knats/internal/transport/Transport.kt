package dimensional.knats.internal.transport

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*

public interface Transport {
    /**
     * Bytes being written to the socket by the NATS server.
     */
    public val incoming: ByteReadChannel

    /**
     * Close the connection.
     */
    public suspend fun close()

    /**
     * Upgrade the connection to a TLS connection if needed.
     */
    public suspend fun upgradeTLS(): Transport

    /**
     *
     */
    public suspend fun write(block: suspend (ByteWriteChannel) -> Unit)

    public suspend fun flush()
}