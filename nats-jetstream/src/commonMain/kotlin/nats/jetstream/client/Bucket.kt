package nats.jetstream.client

import nats.core.protocol.*
import nats.jetstream.api.JetStreamApiException
import nats.jetstream.entity.behavior.StreamBehavior
import nats.jetstream.protocol.domain.KeyValueOperation

@Suppress("MemberVisibilityCanBePrivate")
public class Bucket(public val store: BucketStore, public val name: String) {
    public val subject: Subject get() = subject(name)

    public val stream: StreamBehavior get() = store.client.streams[prefix(name)]

    /**
     * Get an entry from this bucket with the given name.
     *
     * @param key The key to get the value for.
     * @return The [BucketEntry] instance for the given key, or `null` if the key was deleted.
     * @throws JetStreamApiException
     */
    public suspend fun get(key: String): BucketEntry? = get0(key)?.takeIf { it.operation == KeyValueOperation.Put }

    /**
     * Get an entry from this bucket with the given key and revision.
     *
     * @param key      The key to get the value for.
     * @param revision The revision of the message.
     * @return The [BucketEntry] instance for the given key, or `null.
     * @throws JetStreamApiException
     */
    public suspend fun get(key: String, revision: Long): BucketEntry? =
        BucketEntry.Info(stream.messages.fetch { sequence(revision) }).takeIf { it.key == key }


    /**
     * Delete an entry from this bucket with the given key.
     *
     * @param key The key to delete.
     * @throws JetStreamApiException If the JetStream API returns an error.
     */
    public suspend fun delete(key: String) {
        stream.messages.publish(subject + key) {
            header("KV-Operation", KeyValueOperation.Delete.code)
        }
    }

    /**
     * Purge all entries for a given key.
     *
     * @param key The key to purge.
     * @throws JetStreamApiException If the JetStream API returns an error.
     */
    public suspend fun purge(key: String) {
        stream.messages.publish(subject + key) {
            header("KV-Operation", KeyValueOperation.Purge.code)
            rollup(RollupSubject.Subject)
        }
    }

    @PublishedApi
    internal suspend fun get0(key: String): BucketEntry.Info? =
        stream.messages.get { lastBySubj(subject + key) }?.let(BucketEntry::Info)

    public companion object {
        public const val STREAM_PREFIX: String = "KV_"
        public val SUBJECT_PREFIX: Subject = Subject("\$KV")

        /**
         * Create a [Subject] for the given [bucket].
         */
        public fun subject(bucket: String): Subject = SUBJECT_PREFIX + bucket

        /**
         * Prefix the given [bucket] with the [STREAM_PREFIX] if it does not already have it.
         */
        public fun prefix(bucket: String): String = if (hasPrefix(bucket)) bucket else "$STREAM_PREFIX$bucket"

        /**
         *
         */
        public fun hasPrefix(stream: String): Boolean = stream.startsWith(STREAM_PREFIX)
    }
}


/**
 * Put an entry into this bucket with the given name.
 *
 * @param key   The key to put the value for.
 * @param block The block to build the publication.
 * @return The sequence number of the message.
 */
public suspend inline fun Bucket.put(
    key: String,
    block: BasePublicationBuilder.() -> Unit
): Long = stream.messages.publish(subject + key, block).seq

/**
 * Put an entry into this bucket with the given [key] if it doesn't exist (no history) or has been
 * deleted (history shows it was deleted).
 *
 * **Note:**
 * The body of the publication may be used twice if the key hasn't been purged. Try to avoid using one-shot
 * [ReadChannel][PublicationBody.ReadChannel] or [Callback][PublicationBody.Callback] bodies.
 *
 * @param key   The key to put the value for.
 */
public suspend inline fun Bucket.set(
    key: String,
    block: BasePublicationBuilder.() -> Unit
): Long {
    try {
        return update(key, 0, block)
    } catch (ex: JetStreamApiException) {
        if (ex.data.errCode == 10071) {
            val latest = get0(key)
            if (latest != null && latest.operation != KeyValueOperation.Put) return update(key, latest.revision, block)
        }

        throw ex
    }
}

/**
 * Put an entry into this bucket with the given [key] if the latest revision matches [revision].
 *
 * @param key      The key to put the value for.
 * @param revision The revision of the message.
 * @param block    The block to build the publication.
 */
public suspend inline fun Bucket.update(
    key: String,
    revision: Long,
    block: BasePublicationBuilder.() -> Unit
): Long = stream.messages.publish(subject + key) {
    block()
    options { expectedLastSequence = revision }
}.seq
