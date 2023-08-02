package dimensional.knats.client

import dimensional.knats.annotations.InternalNatsApi
import dimensional.knats.connection.Connection
import dimensional.knats.connection.ConnectionImpl
import dimensional.knats.protocol.Delivery
import dimensional.knats.protocol.Operation
import dimensional.knats.protocol.Publication
import dimensional.knats.protocol.withReplyTo
import dimensional.knats.subscription.Subscription
import dimensional.knats.subscription.SubscriptionImpl
import dimensional.knats.tools.NUID
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import naibu.concurrency.ConcurrentHashMap
import naibu.ext.into
import kotlin.collections.set

internal data class ClientImpl(override val resources: ClientResources) : Client {
    internal val mutableSubscriptions = ConcurrentHashMap<String, SubscriptionImpl>()
    internal val awaitingResponses = ConcurrentHashMap<String, CompletableDeferred<Delivery>>()
    internal val conn = ConnectionImpl(resources)

    internal val mainInbox = resources.createInbox() + ".*"
//    internal var inboxDspt by atomic<Dispatcher?>(null)

    @InternalNatsApi
    override val connection: Connection get() = conn
    override val subscriptions: Map<String, Subscription> get() = mutableSubscriptions

    override suspend fun connect() {
        /* connect to the remote NATS server. */
        conn.connect()

        /* subscribe to the request-reply inbox */
        conn.send(Operation.Sub(mainInbox, null, resources.nuid.next()))

        /* process all received messages from the connection. */
        conn.operations
            .filterIsInstance<Delivery>()
            .onEach { awaitingResponses[it.subject.responseToken]?.complete(it) }
            .onEach { mutableSubscriptions[it.sid]?.emit(it) }
            .launchIn(conn.scope)
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

    override suspend fun request(publication: Publication): Delivery {
        val inbox = mainInbox.createResponseInbox()
        val response = CompletableDeferred<Delivery>()
        awaitingResponses[inbox.responseToken] = response
        publish(publication.withReplyTo(inbox))
        return response.await()
    }

//    internal fun createDispatcher(block: suspend (Delivery) -> Unit): Dispatcher {
//        val dspt = Dispatcher(this)
//        dspt.start(block)
//        return dspt
//    }

    internal fun String.createResponseInbox(): String =
        substring(0, resources.inboxLength + 1) + resources.nuid.next()

    internal val String.responseToken: String
        get() {
            val len = resources.inboxLength + 1
            return if (length <= len) this else substring(len)
        }

//    internal class Dispatcher(val client: ClientImpl) {
//        private lateinit var job: Job
//        private val subscriptions = ConcurrentHashSet<String>()
//
//        fun start(callback: suspend (Delivery) -> Unit) {
//            require (!::job.isInitialized) {
//                "This dispatcher has already been started."
//            }
//
//            job = client.conn.operations
//                .filterIsInstance<Delivery>()
//                .filter { it.subject in subscriptions }
//                .onEach(callback)
//                .launchIn(client.conn.scope)
//        }
//
//        suspend fun unsubscribe(subject: String, after: Int? = null) {
//            client.conn.send(Operation.Unsub(subject, after))
//        }
//
//        suspend fun subscribe(subject: String) {
//            client.conn.send(Operation.Sub(subject, null, client.resources.nuid.next()))
//        }
//    }
}
