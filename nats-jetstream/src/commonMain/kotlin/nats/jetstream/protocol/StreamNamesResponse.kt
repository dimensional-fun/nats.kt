package nats.jetstream.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("io.nats.jetstream.api.v1.stream_names_response")
public data class StreamNamesResponse(
    val streams: List<String>,
    override val total: Int,
    override val offset: Int,
    override val limit: Int,
) : Response, PaginationResponse
