import dimensional.knats.connection.NatsConnection
import dimensional.knats.protocol.Message
import dimensional.knats.protocol.Operation
import io.ktor.util.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import naibu.ext.print
import kotlin.time.Duration.Companion.seconds

suspend fun main(): Unit = coroutineScope {
    val connection = NatsConnection()
    connection.operations.onEach { op ->
        when (op) {
            is Message -> {
                val payload = op.getPayload()
                println("[${op.sid}] received message on '${op.subject}': ${payload?.readText() ?: "--NO CONTENT--"}")

                op.headers
                    ?.takeUnless { it.isEmpty() }
                    ?.flattenEntries()
                    ?.joinToString("\n") { "\t- ${it.first}: ${it.second}" }
                    ?.print()
            }

            else -> {}
        }
    }.launchIn(this)

    connection.connect()
    delay(0.5.seconds)
    connection.send(Operation.Sub(">", null, "1"))
}


