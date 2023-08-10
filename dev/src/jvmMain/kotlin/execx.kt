import dimensional.knats.annotations.InternalNatsApi
import dimensional.knats.client.Client
import dimensional.knats.client.publish
import dimensional.knats.protocol.payload
import dimensional.knats.subscription.event.SubscriptionDeliveryEvent
import dimensional.knats.subscription.on
import dimensional.knats.transport.TcpTransport
import io.nats.client.Nats
import io.nats.client.impl.Headers
import kotlinx.coroutines.job
import naibu.ext.print

public suspend fun main() {
    jnats()
    knats()
}

@OptIn(InternalNatsApi::class)
public suspend fun knats() {
    val conn = Client("nats://127.0.0.1") {
        transport = TcpTransport
    }

    "lol".print()

//    conn.subscribe("greet").on<SubscriptionDeliveryEvent> {
//        reply("Hello, ${delivery.readText()}")
//    }

    val dspt = conn.dispatcher()

    dspt.start {
        conn.publish(it.replyTo ?: error("fuck you")) {
            payload("Hello, ${it.readText()}")
        }
    }

    dspt.subscribe("greet")

    conn.connection.scope.coroutineContext.job.join()
}

public fun jnats() {
    val conn = Nats.connect()
    "connected to nats".print()

    val lol = conn.createDispatcher {
        val headers = Headers()
        headers.put("content-type", "text/plain; charset=utf-8")

        conn.publish(it.replyTo, headers, "Hello, ${it.data.decodeToString()}".toByteArray())
    }

    lol.subscribe("greet")
}
