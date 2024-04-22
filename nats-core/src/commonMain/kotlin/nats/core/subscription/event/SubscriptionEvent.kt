package nats.core.subscription.event

import nats.core.client.Client
import nats.core.subscription.Subscription

public sealed interface SubscriptionEvent {
    /**
     * The subscription that emitted this event.
     */
    public val subscription: Subscription

    /**
     * The client that created the [subscription].
     */
    public val client: Client get() = subscription.client
}