package nats.jetstream.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nats.jetstream.protocol.domain.StreamMessage

@Serializable
@SerialName("io.nats.jetstream.api.v1.stream_msg_get_response")
public data class StreamMessageGetResponse(val message: StreamMessage) : Response
