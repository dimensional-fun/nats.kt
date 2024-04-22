package nats.core.subscription

import nats.core.client.Client
import nats.core.subscription.event.SubscriptionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import nats.core.protocol.Subject

public interface Subscription {
    public val scope: CoroutineScope

    /**
     * The unique identifier of this subscription.
     */
    public val id: String

    /**
     * The subject this subscription is listening on.
     */
    public val subject: Subject

    /**
     * The queue group this subscription is part of.
     */
    public val queueGroup: String?

    /**
     * The client that created this subscription.
     */
    public val client: Client

    /**
     * The state of this subscription.
     */
    public val state: StateFlow<SubscriptionState>

    /**
     * The events emitted by this subscription.
     */
    public val events: SharedFlow<SubscriptionEvent>

    /**
     * The number of messages received by this subscription.
     */
    public val received: Long

    /**
     * The number of messages dropped by this subscription.
     */
    public val dropped: Long

    /**
     * Cancel this subscription, or if [after] is provided, cancel after [n][after] messages have been received.
     *
     * @param after The number of messages to receive for cancelling.
     */
    public suspend fun unsubscribe(after: Int? = null)

    /**
     * Resubscribes to the subject & queue group.
     */
    public suspend fun resubscribe()

    /**
     * Cancels this subscription & prevents it from being used afterward.
     */
    public suspend fun detach()
}