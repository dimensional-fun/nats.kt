import dimensional.knats.client.Client
import dimensional.knats.client.request
import dimensional.knats.protocol.isNoResponders
import dimensional.knats.protocol.payload
import dimensional.knats.transport.TransportFactory
import kotlinx.coroutines.coroutineScope
import naibu.ext.print
import naibu.monads.unwrapOr
import naibu.platform.Environment
import kotlin.time.measureTimedValue

public val NATS_SERVER_ADDR: String = Environment["NATS_SERVER_ADDR"].unwrapOr("127.0.0.1:4222")

public suspend fun test(transport: TransportFactory): Unit = coroutineScope {
    val client = Client("nats://$NATS_SERVER_ADDR") {
        this.transport = transport
    }

    client.request("\$JS.API.INFO")
        .readText()
        .print()


//    launch { greeter(client) }
//
//    while (true) {
//        delay(500.milliseconds)
//        client.sendGreet("Gino")
//    }
}

public suspend fun Client.sendGreet(name: String): String? {
    val (reply, took) = measureTimedValue {
        request("greet") { payload(name) }
    }

    if (reply.isNoResponders) {
        return null
    }

    took.print()
    return reply.readText()
}

