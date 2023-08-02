package nats.jetstream.protocol

import kotlinx.serialization.Serializable

@Serializable
public data class Error(
    /**
     * HTTP like error code in the 300 to 500 range
     */
    val code: Int,
    /**
     * A human friendly description of the error
     */
    val description: String? = null,
    /**
     * The NATS error code unique to each kind of error
     */
    val errCode: Int? = null,
)