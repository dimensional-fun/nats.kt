package nats.jetstream.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("io.nats.jetstream.api.v1.stream_delete_response")
public data class StreamDeleteResponse(val success: Boolean) : Response
