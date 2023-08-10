import dimensional.knats.client.ClientBuilder
import dimensional.knats.client.ClientResources
import dimensional.knats.protocol.DefaultOperationParser
import dimensional.knats.tools.NUID
import dimensional.knats.tools.toServerAddr
import dimensional.knats.transport.TcpTransport
import dimensional.kyuso.tools.Runnable
import io.ktor.http.*
import kotlinx.coroutines.Deferred

public data class Bench1(
    val numMsgs: Int = 5_000_00,
    val numPubs: Int = 1,
    val nubSubs: Int = 0,
    val size: Int = 128,
    val urls: List<String> = listOf("nats://127.0.0.1:4222"),
    val subject: String = NUID.next(),
    val csv: Boolean = false,
    val stats: Boolean = false,
    val secure: Boolean = false,
) {
    public fun toResources(): ClientResources = ClientResources(
        urls.map(::Url).map { it.toServerAddr() },
        TcpTransport,
        ClientBuilder.DEFAULT_INBOX_PREFIX,
        DefaultOperationParser,
        null,
        NUID
    )

    public class Worker (public val start: Deferred<Boolean>): Runnable<Unit> {
        override suspend fun run() {
        }
    }
}
