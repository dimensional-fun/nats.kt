import dimensional.knats.connection.NatsConnection
import dimensional.knats.connection.NatsResources
import dimensional.knats.connection.transport.TcpTransport
import dimensional.knats.protocol.Message
import dimensional.knats.protocol.NatsServerAddress
import dimensional.knats.protocol.Operation
import io.ktor.util.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.job
import naibu.common.generateUniqueId
import naibu.ext.print
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

public suspend fun bruh(): Unit = coroutineScope {
    delay(500.milliseconds)

    val connection = NatsConnection(
        NatsResources(listOf(NatsServerAddress("127.0.0.1", 4222)), TcpTransport)
    )

    measureTime { connection.connect() }.print()
    connection.send(Operation.Sub(">", null, generateUniqueId()))

    val received = atomic(0)
    connection.operations
        .filterIsInstance<Message>()
        .collect { msg ->
            val id = received.incrementAndGet()

            //
            val payload = msg.getPayload()
            println("[$id] Received on \"${msg.subject}\"")

            msg.headers
                ?.takeUnless { it.isEmpty() }
                ?.flattenEntries()
                ?.joinToString("\n", postfix = "\n") { "${it.first}: ${it.second}" }
                ?.print()

            println(payload?.readText() ?: "--NO CONTENT--")
            println()
        }

    connection.scope.coroutineContext.job.join()
}

//public suspend fun NatsConnection.subscribe(subject: String, queue: String? = null): NatsSubscription {
//    val id = generateUniqueId()
//    send(Operation.Sub(subject, queue, id))
//
//    return NatsSubscription(this, id)
//}

//public data class NatsSubscription(val connection: NatsConnection, val id: String) {
//    internal val received = atomic(0L)
//    internal var max: Int by Delegates.vetoable(-1) { _, o, _ -> o == -1 }
//
//    val scope: CoroutineScope = connection.scope.child(CoroutineName("Subscription [$id]"), false)
//
//    /**
//     *
//     */
//    val messages: SharedFlow<Message> = connection.operations
//        .filterIsInstance<Message>()
//        .filter { it.sid == id }
//        .takeWhile { if (max == -1) true else received.getAndIncrement() < max }
//        .shareIn(scope, SharingStarted.Lazily)
//
//    public fun on(scope: CoroutineScope = this.scope, block: suspend (Message) -> Unit): Job {
//        return messages.onEach { scope.launch { block(it) } }.launchIn(scope)
//    }
//
//    /**
//     * Unsubscribe from any active subscriptions.
//     */
//    public suspend fun unsubscribe(after: Int? = null) {
//        connection.send(Operation.Unsub(id, after))
//        if (after != null) max = after
//    }
//}
