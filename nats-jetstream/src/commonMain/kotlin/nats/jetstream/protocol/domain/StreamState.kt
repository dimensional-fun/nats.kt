package nats.jetstream.protocol.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nats.core.protocol.optional.Optional
import nats.core.protocol.optional.OptionalLong

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
    val firstTimestamp: Optional<String> = Optional.Missing(),
    /**
     * The sequence number of the last message in the Stream.
     */
    @SerialName("last_seq")
    val lastSequence: ULong,
    /**
     * The timestamp of the last message in the Stream
     */
    @SerialName("last_ts")
    val lastTimestamp: Optional<Instant> = Optional.Missing(),
    /**
     * IDS of messages that were deleted using the Message Delete API or Interest based streams removing messages out
     * of order.
     */
    val deleted: Optional<List<ULong>> = Optional.Missing(),
    /**
     * Subjects and their message counts when a subjects_filter was set
     */
    val subjects: Optional<Map<String, ULong>> = Optional.Missing(),
    /**
     * The number of unique subjects held in the stream
     */
    @SerialName("num_subjects")
    val numSubjects: OptionalLong = OptionalLong.Missing,
    /**
     * The number of deleted messages
     */
    @SerialName("num_deleted")
    val numDeleted: OptionalLong = OptionalLong.Missing,
    /**
     * Records messages that were damaged and unrecoverable
     */
    val lost: Optional<Lost> = Optional.Missing(),
    /**
     * Number of Consumers attached to the Stream
     */
    @SerialName("consumer_count")
    val consumerCount: OptionalLong = OptionalLong.Missing,
) {
    @Serializable
    public data class Lost(
        /**
         * The messages that were lost
         */
        val msgs: Optional<List<ULong>> = Optional.Missing(),
        /**
         * The number of bytes that were lost
         */
        val bytes: Optional<ULong> = Optional.Missing(),
    )
}
