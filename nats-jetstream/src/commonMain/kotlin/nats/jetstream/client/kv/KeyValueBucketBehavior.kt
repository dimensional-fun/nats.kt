package nats.jetstream.client.kv

import nats.core.protocol.*
import nats.jetstream.api.JetStreamApiException
import nats.jetstream.api.catchNotFound
import nats.jetstream.client.stream.StreamBehavior
import nats.jetstream.client.stream.fetch
import nats.jetstream.client.stream.get
import nats.jetstream.client.stream.publish
import nats.jetstream.protocol.domain.KeyValueOperation

public interface KeyValueBucketBehavior {
    public val kv: KeyValueClient

    public val name: String

    public suspend fun resolve(): KeyValueBucket

    public suspend fun resolveOrNull(): KeyValueBucket?

    public suspend fun fetch(): KeyValueBucket {
        val stream = stream.fetch()
        return KeyValueBucket(kv, name, KeyValueBucketInfo(stream.info))
    }

    public suspend fun fetchOrNull(): KeyValueBucket? = catchNotFound { fetch() }

    /**
     * Delete this entire bucket.
     */
    public suspend fun delete() {
        stream.delete()
    }

    /**
     * Delete this entire bucket.
     *
     * @return `true` if the bucket was deleted, `false` if it did not exist.
     * @throws JetStreamApiException if an error occurs while deleting the bucket.
     */
    public suspend fun remove(): Boolean = stream.remove()

    /**
     * Get an entry from this bucket with the given name.
     *
     * @param key The key to get the value for.
     * @return The [KeyValueBucketEntry] instance for the given key, or `null` if the key was deleted.
     * @throws JetStreamApiException
     */
    public suspend fun get(key: String): KeyValueBucketEntry? {
        fetch()
        return get0(key)?.takeIf { it.operation == KeyValueOperation.Put }
    }

    /**
     * Get an entry from this bucket with the given key and revision.
     *
     * @param key      The key to get the value for.
     * @param revision The revision of the message.
     * @return The [KeyValueBucketEntry] instance for the given key, or `null.
     * @throws JetStreamApiException
     */
    public suspend fun get(key: String, revision: Long): KeyValueBucketEntry? {
        fetch()
        return stream.messages.get { sequence(revision) }
            ?.let(KeyValueBucketEntry::Info)
            ?.takeIf { it.key == key }
    }


    /**
     * Delete an entry from this bucket with the given key.
     *
     * @param key The key to delete.
     * @throws JetStreamApiException If the JetStream API returns an error.
     */
    public suspend fun delete(key: String) {
        fetch()
        stream.messages.publish(subject + key) {
            header("KV-Operation", KeyValueOperation.Delete.code)
        }
    }

    /**
     * Delete an entry from this bucket with the given key.
     *
     * @param key The key to delete.
     * @return `true` if the key was deleted, `false` if the key did not exist.
     * @throws JetStreamApiException If the JetStream API returns an error.
     */
    public suspend fun remove(key: String): Boolean {
        fetch()
        return catchNotFound { delete(key) } != null
    }

    /**
     * Purge all entries for a given key.
     *
     * @param key The key to purge.
     * @throws JetStreamApiException If the JetStream API returns an error.
     */
    public suspend fun purge(key: String) {
        fetch()
        stream.messages.publish(subject + key) {
            header("KV-Operation", KeyValueOperation.Purge.code)
            rollup(RollupSubject.Subject)
        }
    }
}

public fun KeyValueBucketBehavior(
    kv: KeyValueClient,
    name: String,
): KeyValueBucketBehavior = object : KeyValueBucketBehavior {
    override val kv: KeyValueClient get() = kv
    override val name: String get() = name

    override suspend fun resolve(): KeyValueBucket = fetch()

    override suspend fun resolveOrNull(): KeyValueBucket? = fetchOrNull()

    override fun toString(): String = "KeyValueBucketBehavior(name=$name, kv=$kv)"

    override fun equals(other: Any?): Boolean = when (other)  {
        is KeyValueBucketBehavior -> other.name == name
        else -> false
    }

    override fun hashCode(): Int = arrayOf(name).contentHashCode()
}

public val KeyValueBucketBehavior.subject: Subject get() = KeyValueBucket.subject(name)

public val KeyValueBucketBehavior.stream: StreamBehavior get() = kv.client.streams[KeyValueBucket.prefix(name)]

@PublishedApi
internal suspend fun KeyValueBucketBehavior.get0(key: String): KeyValueBucketEntry.Info? =
    stream.messages.get { lastBySubj(subject + key) }?.let(KeyValueBucketEntry::Info)

/**
 * Put an entry into this bucket with the given name.
 *
 * @param key   The key to put the value for.
 * @param block The block to build the publication.
 * @return The sequence number of the message.
 */
public suspend inline fun KeyValueBucketBehavior.put(
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
public suspend inline fun KeyValueBucketBehavior.set(
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
public suspend inline fun KeyValueBucketBehavior.update(
    key: String,
    revision: Long,
    block: BasePublicationBuilder.() -> Unit
): Long = stream.messages.publish(subject + key) {
    block()
    options { expectedLastSequence = revision }
}.seq
