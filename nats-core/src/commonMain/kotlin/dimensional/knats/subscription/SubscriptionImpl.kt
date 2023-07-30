package dimensional.knats.subscription

import dimensional.knats.client.ClientImpl
import dimensional.knats.protocol.Delivery
import dimensional.knats.protocol.Operation
import dimensional.knats.subscription.event.SubscriptionDeliveryEvent
import dimensional.knats.subscription.event.SubscriptionEvent
import dimensional.knats.subscription.event.SubscriptionUnsubscribedEvent
import dimensional.knats.tools.child
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*

internal data class SubscriptionImpl(
    override val client: ClientImpl,
    override val id: String,
    override val subject: String,
    override val queueGroup: String?,
) : Subscription {
    override val scope: CoroutineScope = client.conn.scope.child(CoroutineName("Subscription [$id]"))

    private val mutableState = MutableStateFlow<SubscriptionState>(SubscriptionState.Active)
    private val mutableEvents = MutableSharedFlow<SubscriptionEvent>(extraBufferCapacity = Int.MAX_VALUE)
    private val mutableDropped = atomic(0L)
    private val mutableReceived = atomic(0L)

    override val events: SharedFlow<SubscriptionEvent> get() = mutableEvents

    override val state: StateFlow<SubscriptionState> = mutableState.asStateFlow()
    override val dropped: Long by mutableDropped
    override val received: Long by mutableReceived

    private fun hasReachedLimits(next: Boolean = false): Boolean =
        maxMessages?.let { received >= it } ?: isCancelled

    internal suspend fun emit(delivery: Delivery) {
        if (isDetached) {
            return
        }

        /* in case some messages were still in the air before the server auto-unsubscribed us, check if
           we've hit our message limit. */
        if (hasReachedLimits()) {
            mutableDropped.getAndIncrement()
            return
        }

        /* emit the delivery event. */
        val msg = SubscriptionDeliveryEvent(
            this,
            mutableReceived.getAndIncrement(),
            delivery
        )

        mutableEvents.emit(msg)

        /* if this has been our last message then emit an unsubscribed event. */
        if (hasReachedLimits()) {
            mutableEvents.emit(SubscriptionUnsubscribedEvent(this, true))
        }
    }

    override suspend fun unsubscribe(after: Int?) {
        mutableState.update { SubscriptionState.Cancelled(after) }
        client.conn.send(Operation.Unsub(id, after))
        if (after == null) {
            mutableEvents.emit(SubscriptionUnsubscribedEvent(this, false))
        }
    }

    override suspend fun resubscribe() {
        require(!isDetached) {
            "This subscription has been detached, use Client#subscribe"
        }

        client.conn.send(Operation.Sub(subject, queueGroup, id))
        mutableState.update { SubscriptionState.Active }
    }

    override suspend fun detach() {
        if (mutableState.value == SubscriptionState.Active) {
            unsubscribe()
        }

        scope.cancel()
        mutableState.update { SubscriptionState.Detached }
        client.mutableSubscriptions.remove(id)
    }
}