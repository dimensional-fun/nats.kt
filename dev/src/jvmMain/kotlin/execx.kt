import io.nats.client.Nats
import nats.core.transport.TcpTransport

public suspend fun main(): Unit {
    val lol = Nats.connect().jetStream()

//    measureTime { lol.getStreamInfo() }.print()
//    measureTime { lol.getStreamInfo() }.print()
//    measureTime { lol.getStreamInfo() }.print()

    test(TcpTransport)
}
