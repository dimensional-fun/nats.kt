import nats.core.client.Client
import nats.core.transport.TransportFactory
import kotlinx.coroutines.coroutineScope
import naibu.ext.print
import naibu.monads.unwrapOr
import naibu.platform.Environment
import nats.jetstream.client.create
import nats.jetstream.client.jetstream

public val NATS_SERVER_ADDR: String = Environment["NATS_SERVER_ADDR"].unwrapOr("127.0.0.1")

public suspend fun test(transport: TransportFactory): Unit = coroutineScope {
    val client = Client("nats://$NATS_SERVER_ADDR") {
        this.transport = transport
    }

    client.jetstream.streams["test"].remove()

    val stream = client.jetstream.streams.create("test") {
        subjects = listOf("test.*")
    }

    val consumer = stream.consumers.create("my-consumer")
    consumer.print()
}
