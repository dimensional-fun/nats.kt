package nats.core.subscription

import nats.core.subscription.event.SubscriptionDeliveryEvent
import nats.core.subscription.event.SubscriptionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import naibu.ext.intoOrNull

public val Subscription.deliveries: Flow<SubscriptionDeliveryEvent>
    get() = events.buffer(Channel.UNLIMITED).filterIsInstance<SubscriptionDeliveryEvent>()

/**
 * The number of messages that this subscription can receive before auto-unsubscribing.
 */
public val Subscription.maxMessages: Int? get() = state.value.intoOrNull<SubscriptionState.Cancelled>()?.after

/**
 * Whether this subscription is active.
 */
public val Subscription.isActive: Boolean get() = state.value == SubscriptionState.Active

/**
 * Whether this subscription has been detached.
 */
public val Subscription.isDetached: Boolean get() = state.value == SubscriptionState.Detached

/**
 * Whether this subscription has been cancelled.
 */
public val Subscription.isCancelled: Boolean get() = state.value is SubscriptionState.Cancelled

/**
 * Method to conveniently listen to each [SubscriptionEvent] received by this [Subscription].
 *
 * @param scope The scope to launch the job in.
 * @param block Callback to run for each received [SubscriptionEvent]
 * @return a [Job] that can be used to stop this listener.
 */
public inline fun <reified E : SubscriptionEvent> Subscription.on(
    scope: CoroutineScope = this.scope,
    noinline block: suspend E.() -> Unit,
): Job = events.buffer(Channel.UNLIMITED)
    .filterIsInstance<E>()
    .onEach { block(it) }
    .launchIn(scope)

///**
// * Method to conveniently listen to each [SubscriptionDeliveryEvent] received by this [Subscription].
// *
// * @param scope The scope to launch the job in.
// * @param block Callback to run for each received [SubscriptionDeliveryEvent]
// * @return a [Job] that can be used to stop this listener.
// */
//public inline fun Subscription.onMessage(
//    scope: CoroutineScope = this.scope,
//    noinline block: suspend SubscriptionDeliveryEvent.() -> Unit,
//): Job = on(scope, block)
