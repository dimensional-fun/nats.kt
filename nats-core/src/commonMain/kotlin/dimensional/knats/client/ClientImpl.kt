package dimensional.knats.client

import dimensional.knats.connection.Connection
import dimensional.knats.annotations.InternalNatsApi
import dimensional.knats.connection.ConnectionImpl
import dimensional.knats.protocol.Delivery
import dimensional.knats.protocol.Operation
import dimensional.knats.protocol.Publication
import dimensional.knats.subscription.Subscription
import dimensional.knats.subscription.SubscriptionImpl
import dimensional.knats.tools.NUID
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import naibu.concurrency.ConcurrentHashMap
import naibu.ext.into

internal data class ClientImpl(val resources: ClientResources) : Client {
    internal val mutableSubscriptions = ConcurrentHashMap<String, SubscriptionImpl>()
    internal val conn = ConnectionImpl(resources)

    @InternalNatsApi
    override val connection: Connection get() = conn
    override val subscriptions: Map<String, Subscription> get() = mutableSubscriptions

    override suspend fun connect() {
        /* connect to the remote NATS server. */
        conn.connect()

        /* process all received messages from the connection. */
        conn.scope.launch {
            conn.operations
                .filterIsInstance<Delivery>()
                .onEach { mutableSubscriptions[it.sid]?.emit(it) }
                .launchIn(this)
        }
    }

    override suspend fun subscribe(subject: String, queueGroup: String?): Subscription =
        SubscriptionImpl(this, NUID.next(), subject, queueGroup).also {
            conn.send(Operation.Sub(subject, queueGroup, it.id))
            mutableSubscriptions[it.id] = it
        }

    override suspend fun subscribe(id: String, subject: String, queueGroup: String?): Subscription =
        SubscriptionImpl(this, id, subject, queueGroup).also {
            conn.send(Operation.Sub(subject, queueGroup, id))
            mutableSubscriptions[id] = it
        }

    override suspend fun publish(publication: Publication) {
        conn.send(publication.into())
    }
}