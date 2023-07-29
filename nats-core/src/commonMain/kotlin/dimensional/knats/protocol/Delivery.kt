package dimensional.knats.protocol

import io.ktor.http.*
import io.ktor.utils.io.core.*

public sealed interface Delivery {
    public val subject: String
    public val sid: String
    public val replyTo: String?
    public val headers: Headers?

    public fun getPayload(): ByteReadPacket?
}