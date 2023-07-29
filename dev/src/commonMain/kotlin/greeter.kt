import dimensional.knats.Client
import dimensional.knats.listen
import dimensional.knats.reply
import kotlinx.coroutines.coroutineScope

public suspend fun greeter(client: Client): Unit = coroutineScope {
    val sub = client.subscribe("greet")

    sub.listen(this) {
        val payload = delivery.getPayload()
        if (payload == null) {
            reply { payload("incorrect payload") }
            return@listen
        }

        reply {
            payload("Hello, ${payload.readText()}")
        }
    }
}