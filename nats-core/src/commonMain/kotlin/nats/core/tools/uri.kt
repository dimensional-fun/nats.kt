package nats.core.tools

import nats.core.NatsServerAddress
import io.ktor.http.*

public fun Url.toServerAddr(): NatsServerAddress {
    require(protocol.name.equals("nats", true)) {
        "The given URI has an unknown scheme, must be 'nats'."
    }

    return NatsServerAddress(host, specifiedPort.takeUnless { it == 0 } ?: 4222)
}