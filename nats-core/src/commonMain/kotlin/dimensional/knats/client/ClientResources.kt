package dimensional.knats.client

import dimensional.knats.transport.TransportFactory
import dimensional.knats.NatsServerAddress
import dimensional.knats.protocol.OperationParser
import dimensional.knats.tools.NUID

public data class ClientResources(
    /**
     * Servers to possibly connect to.
     */
    val servers: List<NatsServerAddress>,

    /**
     *
     */
    val transportFactory: TransportFactory,

    /**
     *
     */
    val inboxPrefix: String,

    /**
     *
     */
    val parser: OperationParser,

    /**
     * The max number of reconnects for a single server.
     */
    val maxReconnects: Int?,

    /**
     * The NUID generator to use.
     */
    val nuid: NUID,
) {
    public companion object;

    /**
     * The length of an inbox created using [createInbox]
     */
    val inboxLength: Int get() = inboxPrefix.length + 22

    /**
     *
     */
    public fun createInbox(): String = inboxPrefix + nuid.next()
}