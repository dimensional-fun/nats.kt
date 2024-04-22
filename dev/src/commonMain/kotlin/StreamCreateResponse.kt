import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nats.jetstream.protocol.Error
import nats.jetstream.protocol.domain.StreamMirrorExternal
import nats.jetstream.protocol.domain.StreamState
import nats.jetstream.tools.DurationAsNanoseconds

/**
 * A response from the JetStream $JS.API.STREAM.CREATE API
 */
@Serializable
@SerialName("io.nats.jetstream.api.v1.stream_create_response")
public data class StreamCreateResponse(
    /**
     * The active configuration for the Stream
     */
    val config: StreamCreateRequest,
    /**
     * Detail about the current State of the Stream
     */
    val state: StreamState,
    /**
     * Timestamp when the stream was created
     */
    val created: String,
    /**
     * Information about an upstream stream source in a mirror
     */
    val mirror: Source? = null,
    /**
     * Streams being sourced into this Stream
     */
    val sources: List<Source>? = null,
    /**
     * List of mirrors sorted by priority
     */
    val alternatives: List<Alternative>? = null,
) {
    @Serializable
    public data class Cluster(
        /**
         * The cluster name
         */
        val name: String? = null,
        /**
         * The server name of the RAFT leader
         */
        val leader: String? = null,
        /**
         * The members of the RAFT cluster
         */
        val replicas: Replicas? = null,
    ) {
        @Serializable
        public data class Replicas(
            /**
             * The server name of the peer
             */
            val name: String,
            /**
             * Indicates if the server is up-to-date and synchronised
             */
            val current: Boolean,
            /**
             * Nanoseconds since this peer was last seen
             */
            val active: DurationAsNanoseconds
            /**
             * Indicates the node is considered offline by the group
             */
            ,
            val offline: Boolean? = null,
            /**
             * How many uncommitted operations this peer is behind the leader
             */
            val lag: Int? = null,
        )
    }

    @Serializable
    public data class Source(
        /**
         * The name of the Stream being replicated
         */
        val name: String,
        /**
         * How many messages behind the mirror operation is
         */
        val lag: Long,
        /**
         * When last the mirror had activity, in nanoseconds. Value will be -1 when there has been no activity.
         */
        val active: DurationAsNanoseconds,
        /**
         * Configuration referencing a stream source in another account or JetStream domain
         */
        val external: StreamMirrorExternal? = null,
        val error: Error? = null,
    )

    @Serializable
    public data class Alternative(
        /**
         * The mirror stream name
         */
        val name: String,
        /**
         * The name of the cluster holding the stream
         */
        val cluster: String,
        /**
         * The domain holding the string
         */
        val domain: String? = null,
    )
}