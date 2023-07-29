package dimensional.knats

import dimensional.knats.internal.NatsClient
import dimensional.knats.internal.NatsResources
import dimensional.knats.internal.transport.TransportFactory
import dimensional.knats.protocol.NatsServerAddress
import dimensional.knats.protocol.OperationParser
import dimensional.knats.protocol.impl.DefaultOperationParser
import io.ktor.http.*

public class ClientBuilder(public val uri: String) {
    /**
     *
     */
    public lateinit var transport: TransportFactory

    /**
     * The number of times the client can reconnect to a particular server.
     */
    public var maxReconnects: Int? = null

    /**
     * The parser to use for this connection.
     */
    public var parser: OperationParser = DefaultOperationParser


    public fun build(): Client {
        require (::transport.isInitialized) {
            "A transport must be specified in order to connect."
        }

        val url = Url(uri)
        require (url.protocol.name.equals("nats", true)) {
            "The given URI has an unknown scheme, must be 'nats'."
        }

        val resources = NatsResources(
            listOf(NatsServerAddress(url.host, url.specifiedPort.takeUnless { it == 0 } ?: 4222)),
            transport,
            parser,
            maxReconnects
        )

        return NatsClient(resources)
    }
}