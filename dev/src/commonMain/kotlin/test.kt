import dimensional.knats.Client
import dimensional.knats.internal.transport.TcpTransport
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

public suspend fun test(): Unit = coroutineScope {
    val client = Client("nats://127.0.0.1:4222") {
        transport = TcpTransport
    }

    launch { greeter(client) }
    launch { listener(client) }
}
