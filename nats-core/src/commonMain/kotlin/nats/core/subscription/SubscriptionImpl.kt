package nats.core.subscription

import nats.core.client.ClientImpl
import nats.core.protocol.Delivery
import nats.core.protocol.Operation
import nats.core.subscription.event.SubscriptionDeliveryEvent
import nats.core.subscription.event.SubscriptionEvent
import nats.core.subscription.event.SubscriptionUnsubscribedEvent
import nats.core.tools.child
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import nats.core.protocol.Subject

internal data class SubscriptionImpl(
    override val client: ClientImpl,
    override val id: String,
    override val subject: Subject,
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