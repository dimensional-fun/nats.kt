package dimensional.knats.transport

import dimensional.knats.NatsServerAddress
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

public class TcpTransport(private val inner: Connection) : Transport, CoroutineScope by inner.socket {
    public companion object : TransportFactory {
        override suspend fun connect(address: NatsServerAddress, context: CoroutineContext): Transport = TcpTransport(
            aSocket(SelectorManager(context))
                .tcp()
                .connect(address.host, address.port)
                .connection()
        )
    }

    private val writeMutex = Mutex()

    override val isClosed: Boolean by inner.socket::isClosed
    override val incoming: ByteReadChannel by inner::input

    override suspend fun close() {
        inner.socket.close()
        inner.socket.awaitClosed()
    }

    override suspend fun upgradeTLS(): TcpTransport = upgradeTlsNative(inner)

    override suspend fun write(block: suspend (ByteWriteChannel) -> Unit): Unit = writeMutex.withLock {
        block(inner.output)
    }

    override suspend fun flush(): Unit = writeMutex.withLock {
        inner.output.flush()
    }
}
