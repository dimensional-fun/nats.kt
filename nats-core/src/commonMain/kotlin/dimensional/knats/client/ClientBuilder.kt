package dimensional.knats.client

import dimensional.knats.transport.TransportFactory
import dimensional.knats.NatsServerAddress
import dimensional.knats.protocol.OperationParser
import dimensional.knats.protocol.DefaultOperationParser
import dimensional.knats.tools.NUID
import io.ktor.http.*
import naibu.ext.withSuffix

public class ClientBuilder(public val uri: String) {
    public companion object {
        public const val DEFAULT_INBOX_PREFIX: String = "_INBOX."
    }

    /**
     *
     */
    public lateinit var transport: TransportFactory

    /**
     *
     */
    public var inboxPrefix: String = DEFAULT_INBOX_PREFIX
        set(value) {
            field = value.withSuffix(".")
        }

    /**
     * The number of times the client can reconnect to a particular server.
     */
    public var maxReconnects: Int? = null

    /**
     * The parser to use for this connection.
     */
    public var parser: OperationParser = DefaultOperationParser

    /**
     * NUID generator to use.
     */
    public var nuid: NUID = NUID()


    public fun build(): Client {
        require(::transport.isInitialized) {
            "A transport must be specified in order to connect."
        }

        val url = Url(uri)
        require(url.protocol.name.equals("nats", true)) {
            "The given URI has an unknown scheme, must be 'nats'."
        }

        val resources = ClientResources(
            listOf(NatsServerAddress(url.host, url.specifiedPort.takeUnless { it == 0 } ?: 4222)),
            transport,
            inboxPrefix,
            parser,
            maxReconnects,
            nuid
        )

        return ClientImpl(resources)
    }
}