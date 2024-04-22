package nats.jetstream.protocol.domain

import kotlinx.serialization.Serializable

@Serializable
public data class ConsumerReplicas(
    /**
     * The server name of the peer
     */
    public val name: String,
    /**
     * Indicates if the server is up-to-date and synchronised
     */
    public val current: Boolean = false,
    /**
     * Nanoseconds since this peer was last seen
     */
    public val active: Int,
    /**
     * Indicates the node is considered offline by the group
     */
    public val offline: Boolean? = false,
    /**
     * How many uncommitted operations this peer is behind the leader
     */
    public val lag: Int? = null,
)