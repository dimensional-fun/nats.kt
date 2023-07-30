package dimensional.knats.subscription.event

import dimensional.knats.protocol.Delivery
import dimensional.knats.protocol.PublicationBuilder
import dimensional.knats.publish
import dimensional.knats.subscription.Subscription

/**
 *
 */
public data class SubscriptionDeliveryEvent(
    override val subscription: Subscription,
    val id: Long,
    val delivery: Delivery,
) : SubscriptionEvent {
    /**
     *
     */
    public suspend inline fun reply(block: PublicationBuilder.() -> Unit): Boolean {
        client.publish(delivery.replyTo ?: return false, block)
        return true
    }
}
