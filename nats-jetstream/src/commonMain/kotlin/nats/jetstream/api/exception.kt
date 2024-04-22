package nats.jetstream.api

import nats.jetstream.protocol.Error

public class JetStreamApiException(public val data: Error) : Exception("${data.description} [${data.errCode}]")

public inline fun <T : Any> catchNotFound(block: () -> T): T? = try {
    block()
} catch (ex: JetStreamApiException) {
    if (ex.data.code != 404) throw ex
    null
}