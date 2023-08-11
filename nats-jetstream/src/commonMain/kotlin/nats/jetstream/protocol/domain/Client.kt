package nats.jetstream.protocol.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("client")
public data class Client(
    /**
     * Timestamp when the client connected
     */
    public val start: String? = null,
    /**
     * Timestamp when the client disconnected
     */
    public val stop: String? = null,
    /**
     * The remote host the client is connected from
     */
    public val host: String? = null,
    /**
     * The internally assigned client ID for this connection
     */
    public val id: String? = null,
    /**
     * The account this user logged in to
     */
    public val acc: String,
    /**
     * The clients username
     */
    public val user: String? = null,
    /**
     * The name presented by the client during connection
     */
    public val name: String? = null,
    /**
     * The programming language library in use by the client
     */
    public val lang: String? = null,
    /**
     * The version of the client library in use
     */
    public val ver: String? = null,
    /**
     * The last known latency between the NATS Server and the Client in nanoseconds
     */
    public val rtt: Int? = null,
    /**
     * The server that the client was connected to
     */
    public val server: String? = null,
    /**
     * The cluster name the server is connected to
     */
    public val cluster: String? = null,
    /**
     * List of alternative clusters that can be used as overflow for resource placement, in RTT
     * order
     */
    public val alts: List<String>? = null,
    /**
     * The JWT presented in the connection
     */
    public val jwt: String? = null,
    /**
     * The public signing key or account identity key used to issue the user
     */
    @SerialName("issuer_key")
    public val issuerKey: String? = null,
    /**
     * The name extracted from the user JWT claim
     */
    @SerialName("name_tag")
    public val nameTag: String? = null,
    /**
     * The kind of client. Can be Client/Leafnode/Router/Gateway/JetStream/Account/System
     */
    public val kind: String? = null,
    /**
     * The type of client. When kind is Client, this contains the type: mqtt/websocket/nats
     */
    @SerialName("client_type")
    public val clientType: String? = null,
    public val tags: List<String>? = null,
)