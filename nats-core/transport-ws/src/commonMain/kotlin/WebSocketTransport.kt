package dimensional.knats.transport

import dimensional.knats.NatsServerAddress
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.websocket.*
import io.ktor.utils.io.*
import io.ktor.websocket.*
import kotlinx.coroutines.isActive
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmInline

public data class WebSocketTransport(val session: DefaultClientWebSocketSession) : Transport {
    @JvmInline
    public value class Factory(public val httpClient: HttpClient) : TransportFactory {
        public constructor(engine: HttpClientEngineFactory<*>) : this(HttpClient(engine) {
            install(WebSockets)
        })

        override suspend fun connect(address: NatsServerAddress, context: CoroutineContext): Transport =
            WebSocketTransport(httpClient.webSocketSession("ws://${address.host}:${address.port}"))
    }

    private val outgoing = session.reader() {
        while (isActive) {
            channel.awaitContent()

            val packet = channel.readRemaining(channel.availableForRead.toLong())
            session.send(Frame.Binary(true, packet))
        }
    }.channel

    override val incoming: ByteReadChannel = session.writer(autoFlush = true) {
        while (!channel.isClosedForWrite) {
            val frame = try {
                session.incoming.receive()
            } catch (ex: CancellationException) {
                channel.close(ex)
                break
            }

            channel.writeFully(frame.data)
        }
    }.channel

    override val isClosed: Boolean get() = !session.isActive

    override suspend fun close() {
        session.close()
    }

    override suspend fun upgradeTLS(): Transport = this

    override suspend fun write(block: suspend (ByteWriteChannel) -> Unit) {
        block(outgoing)
        outgoing.flush()
    }

    override suspend fun flush() {
        session.flush()
    }
}
