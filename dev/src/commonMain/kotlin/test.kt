import dimensional.knats.internal.NatsClient
import dimensional.knats.internal.NatsResources
import dimensional.knats.internal.transport.TcpTransport
import dimensional.knats.protocol.NatsServerAddress
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

public suspend fun bruh(): Unit = coroutineScope {
    val client = NatsClient(NatsResources(listOf(NatsServerAddress("127.0.0.1", 4222)), TcpTransport))
    client.connect()

    launch { greeter(client) }
    launch { listener(client) }
}
