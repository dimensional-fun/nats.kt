package nats.jetstream.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("io.nats.jetstream.api.v1.consumer_delete_response")
public data class ConsumerDeleteResponse(val success: Boolean): Response
