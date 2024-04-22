package nats.jetstream.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nats.jetstream.protocol.domain.StreamInfo

@Serializable
@SerialName("io.nats.jetstream.api.v1.stream_list_response")
public data class StreamListResponse(
    val streams: List<StreamInfo>,
    /**
     * In clustered environments gathering Stream info might time out, this list would be a list of Streams for which
     * information was not obtainable.
     */
    val missing: List<String> = emptyList(),
    override val total: Int,
    override val offset: Int,
    override val limit: Int,
) : Response, PaginationResponse
