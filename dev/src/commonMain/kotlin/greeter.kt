import nats.core.client.Client
import nats.core.subscription.event.SubscriptionDeliveryEvent
import nats.core.subscription.event.SubscriptionUnsubscribedEvent
import nats.core.subscription.on
import kotlinx.coroutines.coroutineScope
import nats.core.protocol.Delivery.Companion.charsetHint
import nats.core.protocol.Subject

public suspend fun greeter(client: Client): Unit = coroutineScope {
    val sub = client.subscribe(Subject("greet"))

    sub.on<SubscriptionUnsubscribedEvent>(this) {
        println("subscription $sub has been unsubscribed.")
    }

    sub.on<SubscriptionDeliveryEvent>(this) {
        delivery.readText(delivery.charsetHint)
            ?.let { reply("Hello, $it!") }
            ?: return@on reply("incorrect payload")
    }
}