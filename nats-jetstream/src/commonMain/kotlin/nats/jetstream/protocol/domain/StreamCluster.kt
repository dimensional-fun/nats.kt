package nats.jetstream.protocol.domain

import kotlinx.serialization.Serializable

@Serializable
public data class StreamCluster(
    /**
     * The server name of the RAFT leader.
     */
    val leader: String,
    /**
     * The cluster name.
     */
    val name: String? = null,
    /**
     * The members of the RAFT cluster.
     */
    val replicas: List<ConsumerReplicas> = emptyList()
)
