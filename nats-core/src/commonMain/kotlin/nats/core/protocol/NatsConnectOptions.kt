package nats.core.protocol

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import naibu.monads.None
import naibu.monads.Option

@Serializable
public data class NatsConnectOptions(
    /**
     * Enables +OK protocol acknowledgements.
     */
    val verbose: Boolean = false,

    /**
     * Enables additional strict format checking, e.g., for property formed subjects.
     */
    val pedantic: Boolean = false,

    /**
     * Indicates whether the client requires a TLS connection.
     */
    @SerialName("tls_required")
    val tlsRequired: Boolean = false,

    /**
     * The user's JWT.
     */
    val jwt: String? = null,

    /**
     * Public ney.
     */
    @SerialName("nkey")
    val nKey: String? = null,

    /**
     * Signed nonce, encoded to Base64 url.
     */
    @SerialName("sig")
    val signature: String? = null,

    /**
     * The name of the client.
     */
    val name: String? = null,

    /**
     * If set to `true`, the server (version 1.2.0+) will not send originating messages from this connection to its own
     * subscriptions. Clients should set this to `true` only for server supporting this feature, which is when proto in
     * the INFO protocol is set to at least 1.
     */
    val echo: Boolean? = null,

    /**
     * The implementation language of the client.
     */
    val lang: String = "Kotlin ${KotlinVersion.CURRENT}",

    /**
     * Whether this client supports dynamic reconfiguration of cluster topology changes.
     */
    val protocol: Protocol = Protocol.Original,

    /**
     * The connection username.
     */
    val user: String? = null,

    /**
     * The connection password.
     */
    val pass: String? = null,

    /**
     * The version of the client.
     */
    val version: String = "unknown",

    /**
     * The client's authorization token (if [nats.core.protocol.NatsInfoOptions.authRequired] is set).
     */
    @SerialName("auth_token")
    val authToken: String? = null,

    /**
     * Whether the client supports headers.
     */
    val headers: Boolean? = null,

    /**
     */
    @SerialName("no_responders")
    val noResponders: Boolean = false
) {
    public companion object;

    @Serializable(with = Protocol.Serializer::class)
    public enum class Protocol {
        /**
         * The client supports original protocol.
         */
        Original,

        /**
         * Sending 1 indicates that the client supports dynamic reconfiguration of cluster topology changes by
         * asynchronously receiving INFO messages with known servers it can reconnect to.
         */
        Dynamic;

        public companion object Serializer : KSerializer<Protocol> {
            override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("io.nats.Protocol", PrimitiveKind.INT)
            override fun serialize(encoder: Encoder, value: Protocol): Unit = encoder.encodeInt(value.ordinal)
            override fun deserialize(decoder: Decoder): Protocol = Protocol.entries[decoder.decodeInt()]
        }
    }
}
