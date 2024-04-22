package nats.jetstream.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nats.core.protocol.optional.Optional
import nats.core.protocol.optional.OptionalBoolean
import nats.core.protocol.optional.OptionalInt
import nats.core.protocol.optional.delegate.delegate

@Serializable
public data class StreamInfoRequest(
    /**
     * When true will result in a full list of deleted message IDs being returned in the info response.
     */
    @SerialName("deleted_details")
    val deletedDetails: OptionalBoolean = OptionalBoolean.Missing,
    /**
     * When set will return a list of subjects and how many messages they hold for all matching subjects. Filter is a
     * standard NATS subject wildcard.
     */
    @SerialName("subjects_filter")
    val subjectsFilter: Optional<String> = Optional.Missing(),
    /**
     * Paging offset when retrieving pages of subject details.
     */
    val offset: OptionalInt = OptionalInt.Missing,
) {
    public class Builder {
        private var _deletedDetails: OptionalBoolean = OptionalBoolean.Missing
        public var deletedDetails: Boolean? by ::_deletedDetails.delegate()

        private var _subjectsFilter: Optional<String> = Optional.Missing()
        public var subjectsFilter: String? by ::_subjectsFilter.delegate()

        private var _offset: OptionalInt = OptionalInt.Missing
        public var offset: Int? by ::_offset.delegate()

        public fun build(): StreamInfoRequest = StreamInfoRequest(
            deletedDetails = _deletedDetails,
            subjectsFilter = _subjectsFilter,
            offset = _offset
        )
    }
}
