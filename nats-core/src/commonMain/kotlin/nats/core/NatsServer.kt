package nats.core

import nats.core.protocol.NatsInfoOptions

public data class NatsServer(
    val address: NatsServerAddress,
    val info: NatsInfoOptions,
)
