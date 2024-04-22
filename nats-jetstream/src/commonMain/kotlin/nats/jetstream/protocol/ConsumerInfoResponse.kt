package nats.jetstream.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nats.jetstream.protocol.domain.ConsumerInfo
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
@SerialName("io.nats.jetstream.api.v1.consumer_info_response")
public value class ConsumerInfoResponse(public val info: ConsumerInfo) : Response {
    public operator fun component1(): ConsumerInfo = info
}
