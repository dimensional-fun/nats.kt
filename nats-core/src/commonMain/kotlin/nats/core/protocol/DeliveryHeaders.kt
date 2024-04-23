package nats.core.protocol

import io.ktor.http.*
import io.ktor.utils.io.core.*
import nats.core.tools.COLON
import nats.core.tools.CR
import nats.core.tools.LF
import nats.core.tools.WHITESPACE
import nats.core.tools.ktor.discardValues
import nats.core.tools.ktor.ensureCRLF
import nats.core.tools.ktor.readUntilDelimiter
import nats.core.tools.ktor.readUntilDelimiters

public data class DeliveryHeaders(val status: String, val headers: Headers) {
    public companion object {
        public fun read(packet: ByteReadPacket): DeliveryHeaders {
            val status = packet.readUntilDelimiters(CR, LF)
            packet.ensureCRLF()

            val headers = HeadersBuilder()
            while (packet.remaining > 2) {
                println(packet.remaining)

                val name = packet.readUntilDelimiter(COLON)
                require (packet.readByte() == COLON) {
                    "The given packet does not contain a colon after the header name."
                }

                packet.discardValues(WHITESPACE)

                val value = packet.readUntilDelimiters(CR, LF)
                packet.ensureCRLF()

                headers.append(name.readText(), value.readText())
            }

            packet.ensureCRLF()
            return DeliveryHeaders(status.readText(), headers.build())
        }
    }
}
