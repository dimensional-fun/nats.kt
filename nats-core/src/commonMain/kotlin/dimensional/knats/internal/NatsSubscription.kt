package dimensional.knats.internal

import dimensional.knats.Message
import dimensional.knats.Subscription
import dimensional.knats.SubscriptionState
import dimensional.knats.protocol.Delivery
import dimensional.knats.protocol.Operation
import dimensional.knats.tools.child
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import naibu.ext.intoOrNull

public data class NatsSubscription(
    override val client: NatsClient,
    override val id: String,
    override val subject: String,
    override val queueGroup: String?,
) : Subscription {
    override val scope: CoroutineScope = client.conn.scope.child(CoroutineName("Subscription [$id]"))

    private val mutableState = MutableStateFlow<SubscriptionState>(SubscriptionState.Active)
    private val mutableMessages = MutableSharedFlow<Message>(extraBufferCapacity = Int.MAX_VALUE)
    private val mutableDropped = atomic(0L)
    private val mutableReceived = atomic(0L)

    override val messages: SharedFlow<Message> get() = mutableMessages

    override val state: StateFlow<SubscriptionState> = mutableState.asStateFlow()
    override val dropped: Long by mutableDropped
    override val received: Long by mutableReceived

    private fun hasReachedLimits(): Boolean =
        mutableState.value.intoOrNull<SubscriptionState.Cancelled>()?.after?.let { received > it } ?: false

    internal suspend fun emit(delivery: Delivery) {
        /* */
        if (mutableState.value == SubscriptionState.Detached) {
            return
        }

        /* */
        if (hasReachedLimits()) {
            mutableDropped.getAndIncrement()
            return
        }

        val id = mutableReceived.getAndIncrement()
        mutableMessages.emit(Message(id, this, delivery))
    }

    override suspend fun cancel(after: Int?) {
        mutableState.update { SubscriptionState.Cancelled(after) }
        client.conn.send(Operation.Unsub(id, after))
    }

    override suspend fun resubscribe() {
        require(mutableState.value != SubscriptionState.Detached) {
            "This subscription has been detached, use Client#subscribe"
        }

        client.conn.send(Operation.Sub(subject, queueGroup, id))
        mutableState.update { SubscriptionState.Active }
    }

    override suspend fun detach() {
        if (mutableState.value == SubscriptionState.Active) {
            cancel()
        }

        scope.cancel()
        mutableState.update { SubscriptionState.Detached }
        client.mutableSubscriptions.remove(id)
    }
}
