package dimensional.knats.connection.transport

import dimensional.knats.protocol.NatsServerAddress
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlin.coroutines.CoroutineContext

public class TcpTransport(internal val inner: Connection) : Transport {
    public companion object : TransportFactory {
        override suspend fun connect(address: NatsServerAddress, context: CoroutineContext): Transport = TcpTransport(
            aSocket(SelectorManager(context))
                .tcp()
                .connect(address.host, address.port)
                .connection()
        )
    }

    override val incoming: ByteReadChannel get() = inner.input

    override suspend fun close() {
        inner.socket.close()
        inner.socket.awaitClosed()
    }

    override suspend fun upgradeTLS(): TcpTransport = upgradeTlsNative(inner)

    override suspend fun write(packet: ByteReadPacket) {
        inner.output.writePacket(packet)
    }

    override suspend fun flush() {
        inner.output.flush()
    }
}
