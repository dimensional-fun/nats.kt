package dimensional.knats.internal

import dimensional.knats.Client
import dimensional.knats.Connection
import dimensional.knats.Subscription
import dimensional.knats.annotations.InternalNatsApi
import dimensional.knats.internal.connection.NatsConnection
import dimensional.knats.protocol.Delivery
import dimensional.knats.protocol.Operation
import dimensional.knats.protocol.Publication
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import naibu.common.generateUniqueId
import naibu.concurrency.ConcurrentHashMap
import naibu.ext.into
import kotlin.collections.set

public data class NatsClient(val resources: NatsResources) : Client {
    internal val mutableSubscriptions = ConcurrentHashMap<String, NatsSubscription>()
    internal val conn = NatsConnection(resources)

    @InternalNatsApi
    override val connection: Connection get() = conn
    override val subscriptions: Map<String, Subscription> get() = mutableSubscriptions

    public suspend fun connect() {
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
        NatsSubscription(this, generateUniqueId(), subject, queueGroup).also {
            conn.send(Operation.Sub(subject, queueGroup, it.id))
            mutableSubscriptions[it.id] = it
        }

    override suspend fun subscribe(id: String, subject: String, queueGroup: String?): Subscription =
        NatsSubscription(this, id, subject, queueGroup).also {
            conn.send(Operation.Sub(subject, queueGroup, id))
            mutableSubscriptions[id] = it
        }

    override suspend fun publish(publication: Publication) {
        conn.send(publication.into())
    }
}
