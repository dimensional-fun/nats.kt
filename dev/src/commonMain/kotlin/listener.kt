import nats.core.client.Client
import nats.core.subscription.deliveries
import nats.core.subscription.event.SubscriptionDeliveryEvent
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import nats.core.protocol.Subject

public suspend fun listener(client: Client): Unit = coroutineScope {
    client.subscribe(Subject(">")).deliveries
        .map { it.format() }
        .onEach(::println)
        .launchIn(this)
}

public fun Headers.appendTo(dst: Appendable) {
    if (isEmpty()) return
    flattenForEach { name, value -> dst.appendLine("$name: $value") }
    dst.appendLine()
}

public fun SubscriptionDeliveryEvent.format(): String = buildString {
    appendLine("[$id] Received on \"${delivery.subject}\"")
    delivery.headers?.appendTo(this)
    appendLine(delivery.getPayload()?.readText() ?: "--NO CONTENT--")
}
