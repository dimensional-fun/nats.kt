import nats.core.client.Client
import nats.core.transport.TransportFactory
import kotlinx.coroutines.coroutineScope
import naibu.ext.print
import naibu.monads.unwrapOr
import naibu.platform.Environment
import naibu.serialization.DefaultFormats
import naibu.text.charset.Charsets
import nats.core.protocol.json
import nats.core.protocol.read
import nats.core.tools.Json
import nats.jetstream.client.jetstream
import nats.jetstream.client.set

public val NATS_SERVER_ADDR: String = Environment["NATS_SERVER_ADDR"].unwrapOr("127.0.0.1")

public suspend fun test(transport: TransportFactory): Unit = coroutineScope {
    val client = Client("nats://$NATS_SERVER_ADDR") {
        this.transport = transport
    }

    val kv = client.jetstream.kv["test"]

    println("delete")
    kv.delete("test")

    println("sets")
    val seq = kv.set("test") {
        json("hello")
    }

    seq.print()

    println("get")
    val lol = kv.get("test", seq)
    lol.print()
    lol?.read<String>(DefaultFormats.Json, Charsets.UTF_8)?.print()
}
