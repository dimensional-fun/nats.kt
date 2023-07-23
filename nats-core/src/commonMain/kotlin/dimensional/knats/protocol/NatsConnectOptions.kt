package dimensional.knats.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class NatsConnectOptions(
    val verbose: Boolean,
    val pedantic: Boolean,
    @SerialName("tls_required") val tlsRequired: Boolean,
    @SerialName("auth_token") val authToken: String?,
    val user: String?,
    val pass: String?,
    val name: String? = null,
    val lang: String,
    val version: String,
    val protocol: Int? = null,
    val echo: Boolean? = null,
    val sig: String?,
    val jwt: String? = null,
    @SerialName("no_responders") val noResponders: Boolean? = null,
    val headers: Boolean? = null,
    @SerialName("nkey") val nKey: String? = null
) {
    public companion object {
        public val DEFAULT: NatsConnectOptions = NatsConnectOptions(
            verbose = true,
            pedantic = true,
            tlsRequired = false,
            authToken = null,
            user = null,
            pass = null,
            name = "KNats",
            lang = "Kotlin ${KotlinVersion.CURRENT}",
            version = "1.0-rc.1",
            protocol = null,
            headers = true,
            sig = null
        )
    }
}
