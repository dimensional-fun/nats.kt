import dimensional.knats.client.Client
import dimensional.knats.subscription.event.SubscriptionDeliveryEvent
import dimensional.knats.subscription.event.SubscriptionUnsubscribedEvent
import dimensional.knats.subscription.on
import kotlinx.coroutines.coroutineScope

public suspend fun greeter(client: Client): Unit = coroutineScope {
    val sub = client.subscribe("greet")

    sub.on<SubscriptionUnsubscribedEvent> {
        println("subscription $sub has been unsubscribed.")
    }

    sub.on<SubscriptionDeliveryEvent>(this) {
        val payload = delivery.getPayload()
        if (payload == null) {
            reply { payload("incorrect payload") }
            return@on
        }

        reply {
            payload("Hello, ${payload.readText()}")
        }
    }

    sub.unsubscribe(5)
}