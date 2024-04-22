package nats.jetstream.protocol

import kotlinx.serialization.Serializable

@Serializable
public data class OffsetRequest(val offset: Int)
