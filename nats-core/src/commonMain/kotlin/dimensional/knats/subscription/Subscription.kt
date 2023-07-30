package dimensional.knats.subscription

import dimensional.knats.client.Client
import dimensional.knats.subscription.event.SubscriptionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

public interface Subscription {
    public val scope: CoroutineScope

    public val id: String

    public val subject: String

    public val queueGroup: String?

    public val client: Client

    public val state: StateFlow<SubscriptionState>

    public val events: SharedFlow<SubscriptionEvent>

    public val received: Long

    public val dropped: Long

    /**
     * Cancel this subscription, or if [after] is provided, cancel after [n][after] messages have been received.
     *
     * @param after The number of messages to receive for cancelling.
     */
    public suspend fun unsubscribe(after: Int? = null)

    /**
     *
     */
    public suspend fun resubscribe()

    /**
     * Cancels this subscription & prevents it from being used afterward.
     */
    public suspend fun detach()
}