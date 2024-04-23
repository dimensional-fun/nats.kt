package nats.jetstream.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nats.core.protocol.Subject
import nats.core.protocol.optional.Optional
import nats.core.protocol.optional.OptionalLong

@Serializable
public data class StreamMessageGetRequest(
    /**
     * Stream sequence number of the message to retrieve, cannot be combined with [lastBySubject]
     */
    @SerialName("seq")
    val sequence: OptionalLong = OptionalLong.Missing,
    /**
     * Retrieves the last message for a given subject, cannot be combined with [sequence]
     */
    @SerialName("last_by_subj")
    val lastBySubject: Optional<Subject> = Optional.Missing(),
    /**
     * Combined with [sequence] gets the next message for a subject with the given sequence or higher.
     */
    @SerialName("next_by_subj")
    val nextBySubject: Optional<Subject> = Optional.Missing()
) {
    /**
     * Check if the request is to retrieve the last message by subject.
     */
    public fun isLastBySubject(): Boolean = lastBySubject is Optional.Value

    public class Builder {
        private var _sequence: OptionalLong = OptionalLong.Missing

        private var _lastBySubject: Optional<Subject> = Optional.Missing()

        private var _nextBySubject: Optional<Subject> = Optional.Missing()

        public fun sequence(value: Long) {
            _sequence = OptionalLong.Value(value)

            //
            _lastBySubject = Optional.Missing()
            _nextBySubject = Optional.Missing()
        }

        /**
         * Build the [StreamMessageGetRequest] object.
         */
        public fun nextBySubj(value: Subject, sequence: Long) {
            _nextBySubject = Optional(value)
            _sequence = OptionalLong.Value(sequence)

            //
            _lastBySubject = Optional.Missing()
        }

        /**
         * Retrieve the last message for the given subject.
         *
         * Cannot be combined with [sequence].
         */
        public fun lastBySubj(value: Subject) {
            _lastBySubject = Optional(value)

            //
            _nextBySubject = Optional.Missing()
            _sequence = OptionalLong.Missing
        }

        public fun build(): StreamMessageGetRequest = StreamMessageGetRequest(
            sequence = _sequence,
            lastBySubject = _lastBySubject,
            nextBySubject = _nextBySubject
        )
    }
}
