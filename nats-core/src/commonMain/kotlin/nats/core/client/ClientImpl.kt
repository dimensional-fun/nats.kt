package nats.core.client

import nats.core.annotations.InternalNatsApi
import nats.core.connection.Connection
import nats.core.connection.ConnectionImpl
import nats.core.subscription.Subscription
import nats.core.subscription.SubscriptionImpl
import nats.core.tools.NUID
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import naibu.concurrency.ConcurrentHashMap
import naibu.ext.into
import nats.core.protocol.*
import nats.core.protocol.withReplyTo
import kotlin.collections.set

internal data class ClientImpl(override val resources: ClientResources) : Client {
    internal val mutableSubscriptions = ConcurrentHashMap<String, SubscriptionImpl>()
    internal val awaitingResponses = ConcurrentHashMap<Subject, CompletableDeferred<Delivery>>()
    internal val conn = ConnectionImpl(resources)

    internal val mainInbox = resources.createInbox() + "*"
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

    override suspend fun subscribe(subject: Subject, queueGroup: String?): Subscription =
        SubscriptionImpl(this, NUID.next(), subject, queueGroup).also {
            conn.send(Operation.Sub(subject, queueGroup, it.id))
            mutableSubscriptions[it.id] = it
        }

    override suspend fun subscribe(id: String, subject: Subject, queueGroup: String?): Subscription =
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

    internal fun Subject.createResponseInbox(): Subject =
        Subject(value.substring(0, resources.inboxLength + 1) + resources.nuid.next())

    internal val Subject.responseToken: Subject
        get() {
            val len = resources.inboxLength + 1
            return Subject(if (value.length <= len) value else value.substring(len))
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
