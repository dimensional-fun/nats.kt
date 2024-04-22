package nats.jetstream.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nats.jetstream.protocol.domain.StreamInfo
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
@SerialName("io.nats.jetstream.api.v1.stream_info_response")
public value class StreamInfoResponse(public val info: StreamInfo) : Response
