package nats.jetstream.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nats.jetstream.protocol.domain.ConsumerInfo

@Serializable
@SerialName("io.nats.jetstream.api.v1.consumer_list_response")
public data class ConsumerListResponse(
    val consumers: List<ConsumerInfo>,
    override val offset: Int,
    override val total: Int,
    override val limit: Int,
) : Response, PaginationResponse
