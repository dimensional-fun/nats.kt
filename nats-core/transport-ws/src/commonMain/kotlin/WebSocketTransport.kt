import dimensional.knats.connection.transport.Transport
import dimensional.knats.connection.transport.TransportFactory
import dimensional.knats.protocol.NatsServerAddress
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmInline

public data class WebSocketTransport(val session: DefaultClientWebSocketSession) : Transport {
    @JvmInline
    public value class Factory(public val httpClient: HttpClient) : TransportFactory {
        override suspend fun connect(address: NatsServerAddress, context: CoroutineContext): Transport =
            WebSocketTransport(httpClient.webSocketSession("ws://${address.host}:${address.port}"))
    }

    /**
     *
     */
    public companion object : TransportFactory by Factory(HttpClient(CIO) {
        install(WebSockets)
    })

    override val incoming: ByteReadChannel = session.incoming.consume {
        val channel = ByteChannel(true)
        session.launch {
            while (isActive) {
                val frame = try {
                    receive()
                } catch (ex: CancellationException) {
                    channel.cancel(ex)
                    break
                }

                channel.writeFully(frame.data)
            }
        }

        channel
    }

    override suspend fun close() {
        session.close()
    }

    override suspend fun upgradeTLS(): Transport = this

    override suspend fun write(packet: ByteReadPacket) {
        session.send(Frame.Binary(true, packet))
    }

    override suspend fun flush() {
        session.flush()
    }
}
