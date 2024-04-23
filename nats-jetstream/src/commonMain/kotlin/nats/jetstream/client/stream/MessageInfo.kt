package nats.jetstream.client.stream

import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.core.*
import kotlinx.datetime.Instant
import nats.core.protocol.Delivery
import nats.core.protocol.DeliveryHeaders
import nats.core.protocol.HasPayload
import nats.core.protocol.Subject
import nats.jetstream.protocol.domain.StreamMessage

public sealed interface MessageInfo : HasPayload {
    /**
     * The name of the stream the message was published to.
     */
    public val stream: String

    /**
     * The message's sequence number.
     */
    public val sequence: Long

    /**
     * The message's headers.
     */
    // This would be a `Headers` object, but the way we strip the Headers of a direct message downcasts to `StringValues`.
    public val headers: StringValues?

    /**
     * The message's subject.
     */
    public val subject: Subject

    /**
     * The time that this message was published.
     */
    public val timestamp: Instant

    /**
     * The sequence number of the last message in the stream.
     */
    public val lastSequence: Long?

    public class Stream(
        override val stream: String,
        @Suppress("MemberVisibilityCanBePrivate")
        public val inner: StreamMessage
    ) : MessageInfo {
        private val data: ByteReadPacket? by lazy {
            inner.data?.decodeBase64Bytes()?.let(::ByteReadPacket)
        }

        private val hdrs by lazy {
            inner.hdrs
                ?.decodeBase64Bytes()
                ?.let(::ByteReadPacket)
                ?.let(DeliveryHeaders::read)
        }

        override val subject: Subject get() = inner.subject

        override val headers: StringValues? get() = hdrs?.headers

        override val sequence: Long get() = inner.seq

        override val timestamp: Instant get() = inner.time

        override val lastSequence: Long? get() = null

        override fun getPayload(): ByteReadPacket? = data?.copy()

        override fun toString(): String = "MessageInfo(stream=$stream, sequence=$sequence, subject=$subject, timestamp=$timestamp, lastSequence=$lastSequence)"
    }

    public class Direct(
        @Suppress("MemberVisibilityCanBePrivate")
        public val inner: Delivery
    ) : MessageInfo {
        private val specialHeaders = (inner.headers ?: Headers.Empty).filter { k, _ -> k in HEADERS }
        private val regularHeaders = (inner.headers ?: Headers.Empty).filter { k, _ -> k !in HEADERS }

        override val headers: StringValues get() = regularHeaders

        override val sequence: Long by lazy {
            specialHeaders[NATS_SEQUENCE]?.toLong() ?: error("No sequence number found in message headers.")
        }

        override val subject: Subject by lazy {
            val subj = specialHeaders[NATS_SUBJECT] ?: error("No subject found in message headers.")
            Subject(subj)
        }

        override val stream: String by lazy {
            specialHeaders[NATS_STREAM] ?: error("No stream name found in message headers.")
        }

        override val timestamp: Instant by lazy {
            val time = specialHeaders[NATS_TIME_STAMP] ?: error("No timestamp found in message headers.")
            Instant.parse(time)
        }

        override val lastSequence: Long? by lazy {
            specialHeaders[NATS_LAST_SEQUENCE]?.toLong()
        }

        override fun getPayload(): ByteReadPacket? = inner.getPayload()

        override fun toString(): String = "MessageInfo(stream=$stream, sequence=$sequence, subject=$subject, timestamp=$timestamp, lastSequence=$lastSequence)"

        public companion object {
            public const val NATS_STREAM: String = "Nats-Stream"
            public const val NATS_SUBJECT: String = "Nats-Subject"
            public const val NATS_SEQUENCE: String = "Nats-Sequence"
            public const val NATS_TIME_STAMP: String = "Nats-Time-Stamp"
            public const val NATS_LAST_SEQUENCE: String = "Nats-Last-Sequence"

            private val HEADERS get() = setOf(NATS_STREAM, NATS_SUBJECT, NATS_SEQUENCE, NATS_TIME_STAMP, NATS_LAST_SEQUENCE)
        }
    }
}
