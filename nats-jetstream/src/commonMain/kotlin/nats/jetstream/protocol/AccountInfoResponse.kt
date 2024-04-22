package nats.jetstream.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nats.core.protocol.optional.Optional
import nats.core.protocol.optional.OptionalBoolean
import nats.core.protocol.optional.OptionalInt

@Serializable
@SerialName("io.nats.jetstream.api.v1.account_info_response")
public data class AccountInfoResponse(
    /**
     * Memory Storage being used for Stream Message storage
     */
    val memory: Int,
    /**
     * File Storage being used for Stream Message storage
     */
    val storage: Int,
    /**
     * Number of active Streams
     */
    val streams: Int,
    /**
     * Number of active Consumers
     */
    val consumers: Int,
    /**
     * The JetStream domain this account is in
     */
    val domain: Optional<String> = Optional.Missing(),
    val limits: Limits,
    val tiers: Optional<Map<String, Tier>> = Optional.Missing(),
    val api: API,
) : Response {
    @Serializable
    public data class Tier(
        /**
         * Memory Storage being used for Stream Message storage
         */
        val memory: Int,
        /**
         * File Storage being used for Stream Message storage
         */
        val storage: Int,
        /**
         * Number of active Streams
         */
        val streams: Int,
        /**
         * Number of active Consumers
         */
        val consumers: Int,
        val limits: Limits,
    )

    @Serializable
    public data class Limits(
        /**
         * The maximum amount of Memory storage Stream Messages may consume
         */
        @SerialName("max_memory")
        val maxMemory: Int,
        /**
         * The maximum amount of File storage Stream Messages may consume
         */
        @SerialName("max_storage")
        val maxStorage: Int,
        /**
         * The maximum number of Streams an account can create
         */
        @SerialName("max_streams")
        val maxStreams: Int,
        /**
         * The maximum number of Consumer an account can create
         */
        @SerialName("max_consumers")
        val maxConsumers: Int,
        /**
         * Indicates if Streams created in this account requires the max_bytes property set
         */
        @SerialName("max_bytes_required")
        val maxBytesRequired: OptionalBoolean = OptionalBoolean.Missing,
        /**
         * The maximum number of outstanding ACKs any consumer may configure
         */
        @SerialName("max_ack_pending")
        val maxAckPending: OptionalInt = OptionalInt.Missing,
        /**
         * The maximum size any single memory stream may be
         */
        @SerialName("memory_max_stream_bytes")
        val memoryMaxStreamBytes: OptionalInt = OptionalInt.Missing,
        /**
         * The maximum size any single storage based stream may be
         */
        @SerialName("storage_max_stream_bytes")
        val storageMaxStreamBytes: OptionalInt = OptionalInt.Missing,
    )

    @Serializable
    public data class API(
        /**
         * Total number of API requests received for this account
         */
        val total: Int,
        /**
         * API requests that resulted in an error response
         */
        val errors: Int,
    )
}