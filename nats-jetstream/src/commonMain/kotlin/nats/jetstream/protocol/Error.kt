package nats.jetstream.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Error(
    /**
     * HTTP like error code in the 300 to 500 range
     */
    public val code: Int,
    /**
     * A human friendly description of the error
     */
    public val description: String? = null,
    /**
     * The NATS error code unique to each kind of error
     */
    @SerialName("err_code")
    public val errCode: Int? = null,
)