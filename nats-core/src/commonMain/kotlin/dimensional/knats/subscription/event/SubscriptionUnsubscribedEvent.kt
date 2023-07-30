package dimensional.knats.subscription.event

import dimensional.knats.subscription.Subscription

/**
 * Emitted whenever the given [subscription] has been auto-unsubscribed (max message limit) or when it has been
 * manually unsubscribed.
 */
public data class SubscriptionUnsubscribedEvent(
    override val subscription: Subscription,
    /**
     * Whether the server auto-unsubscribed us (max message limit).
     */
    val auto: Boolean,
) : SubscriptionEvent
