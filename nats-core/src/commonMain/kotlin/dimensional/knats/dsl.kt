package dimensional.knats

import dimensional.knats.annotations.InternalNatsApi
import dimensional.knats.internal.connection.NatsConnectionState
import dimensional.knats.protocol.Publication
import dimensional.knats.protocol.PublicationBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Whether this connection is currently connected.
 */
@OptIn(InternalNatsApi::class)
public val Connection.isConnected: Boolean get() = state.value is NatsConnectionState.Connected

/**
 * Whether this connection has been detached.
 */
@OptIn(InternalNatsApi::class)
public val Connection.isDetached: Boolean get() = state.value == NatsConnectionState.Detached

/**
 * The subscription which received this message.
 */
public val Message.client: Client get() = subscription.client

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
 * Method to conveniently listen to each [Message] received by this [Subscription].
 *
 * @param scope The scope to launch the job in.
 * @param block Callback to run for each received [Message]
 * @return a [Job] that can be used to stop this listener.
 */
public inline fun Subscription.listen(
    scope: CoroutineScope = this.scope,
    noinline block: suspend Message.() -> Unit,
): Job =
    messages.buffer(Channel.UNLIMITED)
        .onEach { scope.launch { block(it) } }
        .launchIn(scope)

/**
 *
 */
public suspend inline fun Client.publish(subject: String, block: PublicationBuilder.() -> Unit): Unit =
    publish(Publication(subject, block))

/**
 *
 */
public suspend inline fun Message.reply(block: PublicationBuilder.() -> Unit): Boolean {
    client.publish(delivery.replyTo ?: return false, block)
    return true
}
