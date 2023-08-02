package nats.jetstream.protocol.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class StreamState(
    /**
     * Number of messages stored in the Stream
     */
    val messages: ULong,
    /**
     * Combined size of all messages in the stream.
     */
    val bytes: ULong,
    /**
     * The sequence number of the first message in the Stream.
     */
    @SerialName("first_seq")
    val firstSequence: ULong,
    /**
     * The timestamp of the first message in the Stream
     */
    @SerialName("first_ts")
    val firstTimestamp: String? = null,
    /**
     * The sequence number of the last message in the Stream.
     */
    @SerialName("last_seq")
    val lastSequence: ULong,
    /**
     * The timestamp of the last message in the Stream
     */
    @SerialName("last_ts")
    val lastTimestamp: String? = null,
    /**
     * IDS of messages that were deleted using the Message Delete API or Interest based streams removing messages out
     * of order.
     */
    val deleted: List<ULong>? = null,
    /**
     * Subjects and their message counts when a subjects_filter was set
     */
    val subjects: Map<String, ULong>? = null,
    /**
     * The number of unique subjects held in the stream
     */
    @SerialName("num_subjects")
    val numSubjects: Long? = null,
    /**
     * The number of deleted messages
     */
    @SerialName("num_deleted")
    val numDeleted: Long? = null,
    /**
     * Records messages that were damaged and unrecoverable
     */
    val lost: Lost? = null,
    /**
     * Number of Consumers attached to the Stream
     */
    @SerialName("consumer_count")
    val consumerCount: Long? = null,
) {
    @Serializable
    public data class Lost(
        /**
         * The messages that were lost
         */
        val msgs: List<ULong>? = null,
        /**
         * The number of bytes that were lost
         */
        val bytes: ULong? = null,
    )
}
