package dimensional.knats.subscription.event

import dimensional.knats.client.Client
import dimensional.knats.subscription.Subscription

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