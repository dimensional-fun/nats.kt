package nats.core.subscription.event

import nats.core.client.publish
import nats.core.protocol.Delivery
import nats.core.protocol.PublicationBuilder
import nats.core.protocol.payload
import nats.core.subscription.Subscription
import naibu.text.charset.Charset
import naibu.text.charset.Charsets
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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
    public suspend fun reply(text: String, range: IntRange = text.indices, charset: Charset = Charsets.UTF_8): Unit =
        reply { payload(text, range, charset) }
}


/**
 *
 */
@OptIn(ExperimentalContracts::class)
public suspend inline fun SubscriptionDeliveryEvent.reply(block: PublicationBuilder.() -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    val replyTo = requireNotNull(delivery.replyTo) {
        "Cannot reply to message that doesn't contain a reply-to subject."
    }

    client.publish(replyTo, block)
}
