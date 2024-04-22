package nats.jetstream.protocol

import kotlinx.serialization.Serializable

/**
 *
 */
@Serializable
public data class ErrorResponse(val error: Error) : Response
