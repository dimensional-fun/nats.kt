package nats.jetstream.client.kv

import kotlinx.datetime.Instant
import nats.core.protocol.HasPayload
import nats.jetstream.client.stream.MessageInfo
import nats.jetstream.protocol.domain.KeyValueOperation
import nats.jetstream.tools.BucketAndKey

public sealed class KeyValueBucketEntry : HasPayload {
    /**
     *
     */
    public abstract val id: BucketAndKey
    /**
     *
     */
    public abstract val created: Instant

    /**
     *
     */
    public abstract val delta: Long

    /**
     *
     */
    public abstract val revision: Long

    /**
     *
     */
    public abstract val operation: KeyValueOperation

    /**
     * The bucket name.
     */
    public val bucket: String get() = id.bucket

    /**
     * The key name.
     */
    public val key: String get() = id.key

    public class Info(public val message: MessageInfo) : HasPayload by message, KeyValueBucketEntry() {
        override val id: BucketAndKey get() = BucketAndKey(message.subject.value)

        override val delta: Long get() = 0

        override val created: Instant get() = message.timestamp

        override val revision: Long get() = message.sequence

        override val operation: KeyValueOperation
            get() = message.headers?.get("KV-Operation")
                ?.let { KeyValueOperation.find(it) }
                ?: KeyValueOperation.Put

        override fun toString(): String = "BucketEntry(bucket=$bucket, key=$key, delta=$delta, created=$created, revision=$revision)"
    }
}
