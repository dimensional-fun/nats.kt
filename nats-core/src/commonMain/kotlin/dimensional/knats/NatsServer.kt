package dimensional.knats

import dimensional.knats.protocol.NatsInfoOptions

public data class NatsServer(
    val address: NatsServerAddress,
    val info: NatsInfoOptions,
)
