package dimensional.knats.protocol

import io.ktor.http.*
import io.ktor.utils.io.core.*

internal class DeliveryBuilder {
    lateinit var subject: String
    lateinit var sid: String
    var replyTo: String? = null
    var headers: Headers? = null
    lateinit var header: String
    var packet: ByteReadPacket? = null

    fun build(): Delivery = headers?.let {
        Operation.MsgWithHeaders(subject, sid, replyTo, it, header, packet)
    } ?: Operation.Msg(subject, sid, replyTo, packet)
}
