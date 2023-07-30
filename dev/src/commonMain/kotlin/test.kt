import dimensional.knats.client.Client
import dimensional.knats.transport.TcpTransport
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import naibu.monads.unwrapOr
import naibu.platform.Environment

public val NATS_SERVER_ADDR: String = Environment["NATS_SERVER_ADDR"].unwrapOr("127.0.0.1:4222")

public suspend fun test(): Unit = coroutineScope {
    val client = Client("nats://127.0.0.1:4222") {
        transport = TcpTransport
    }

    launch { greeter(client) }
    launch { listener(client) }
}
