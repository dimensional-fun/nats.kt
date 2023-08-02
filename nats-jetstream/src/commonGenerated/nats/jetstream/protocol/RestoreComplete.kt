// DO NOT EDIT THIS FILE! This was generated by the `./gradlew :generateJetStreamClasses` task.`
package nats.jetstream.protocol

import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("io.nats.jetstream.advisory.v1.restore_complete")
public data class RestoreComplete(
    public val type: String,
    /**
     * Unique correlation ID for this event
     */
    public val id: String,
    /**
     * The time this event was created in RFC3339 format
     */
    public val timestamp: String,
    /**
     * The Stream being restored
     */
    public val stream: String,
    /**
     * The time the Restore process started
     */
    public val start: String,
    /**
     * The time the Restore was completed
     */
    public val end: String,
    /**
     * The number of bytes that was received
     */
    public val bytes: Int,
    /**
     * Details about the client that connected to the server
     */
    public val client: Client,
) {
    @Serializable
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
        /**
         * Tags extracted from the JWT
         */
        public val tags: List<String>? = null,
    )
}
