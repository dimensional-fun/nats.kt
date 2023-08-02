package dimensional.knats.subscription.event

import dimensional.knats.client.publish
import dimensional.knats.protocol.Delivery
import dimensional.knats.protocol.PublicationBuilder
import dimensional.knats.protocol.payload
import dimensional.knats.subscription.Subscription
import naibu.text.charset.Charset
import naibu.text.charset.Charsets

/**
 *
 */
public data class SubscriptionDeliveryEvent(
    override val subscription: Subscription,
    val id: Long,
    val delivery: Delivery,
) : SubscriptionEvent {
    /**
     * @return `true` if this delivery could be replied to.
     */
    public suspend fun reply(text: String, range: IntRange = text.indices, charset: Charset = Charsets.UTF_8): Boolean =
        reply { payload(text, range, charset) }

    /**
     *
     */
    public suspend inline fun reply(block: PublicationBuilder.() -> Unit): Boolean {
        client.publish(delivery.replyTo ?: return false, block)
        return true
    }
}
