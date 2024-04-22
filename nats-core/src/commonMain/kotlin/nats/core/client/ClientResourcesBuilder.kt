package nats.core.client

import nats.core.protocol.DefaultOperationParser
import nats.core.protocol.OperationParser
import nats.core.tools.NUID
import nats.core.tools.toServerAddr
import nats.core.transport.TransportFactory
import io.ktor.http.*
import naibu.ext.withSuffix

public class ClientResourcesBuilder(public val uri: String) {
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

    public fun build(): ClientResources {
        require(::transport.isInitialized) {
            "A transport must be specified in order to connect."
        }

        return ClientResources(
            listOf(Url(uri).toServerAddr()),
            transport,
            inboxPrefix,
            parser,
            maxReconnects,
            nuid
        )
    }
}