package dimensional.knats.internal

import dimensional.knats.internal.transport.TransportFactory
import dimensional.knats.protocol.NatsServerAddress
import dimensional.knats.protocol.OperationParser
import dimensional.knats.protocol.impl.DefaultOperationParser

public data class NatsResources(
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
    val parser: OperationParser = DefaultOperationParser,

    /**
     * The max number of reconnects for a single server.
     */
    val maxReconnects: Int? = null,
)
