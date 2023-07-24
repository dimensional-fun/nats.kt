package dimensional.knats.connection.transport

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlin.coroutines.coroutineContext
import kotlin.jvm.JvmInline

@JvmInline
public value class TcpTransport(public val inner: Connection) : Transport {
    public companion object {
        public suspend fun connect(host: String, port: Int): TcpTransport = TcpTransport(
            aSocket(SelectorManager(coroutineContext))
                .tcp()
                .connect(host, port)
                .connection()
        )
    }

    override fun close() {
        require(::inner.isInitialized) {
            "This transport has not been connected."
        }

        inner.socket.close()
    }

    override suspend fun write(packet: ByteReadPacket) {
        inner.output.writePacket(packet)
        inner.output.flush()
    }

    override suspend fun read(): ByteReadPacket {
        inner.input.awaitContent()
        return inner.input.readPacket(inner.input.availableForRead)
    }
}