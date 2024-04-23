package nats.jetstream.client.kv

import naibu.ext.print
import nats.jetstream.api.JetStreamApiException
import nats.jetstream.client.JetStreamClient
import nats.jetstream.client.stream.create
import nats.jetstream.client.stream.list
import nats.jetstream.client.stream.names
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

@JvmInline
public value class KeyValueClient(public val client: JetStreamClient) {
    /**
     * Get a [KeyValueBucket] instance for the given [bucket name][bucket] regardless of whether it exists.
     *
     * @param bucket The name of the bucket.
     */
    public operator fun get(bucket: String): KeyValueBucketBehavior = KeyValueBucketBehavior(this, bucket)

    /**
     * Get the names of all [buckets][KeyValueBucket]s in the JetStream cluster.
     *
     * @throws JetStreamApiException If an error occurs while fetching the list of buckets.
     */
    public suspend fun names(): List<String> =
        client.streams.names().filter(KeyValueBucket::hasPrefix)

    /**
     * List all [buckets][KeyValueBucket] in the JetStream cluster.
     *
     * @param offset The offset to start listing buckets from.
     * @throws JetStreamApiException If an error occurs while fetching the list of buckets.
     */
    public suspend fun list(offset: Int = 0): List<KeyValueBucket> =
        client.streams.list { this.offset = offset }
            .filter { KeyValueBucket.hasPrefix(it.name) }
            .map { KeyValueBucket(this, it.name, KeyValueBucketInfo(it.info)) }

    @OptIn(ExperimentalContracts::class)
    public suspend inline fun create(name: String, crossinline block: KeyValueBucketConfiguration.Builder.() -> Unit = {}): KeyValueBucket {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

        val stream = client.streams.create(KeyValueBucket.prefix(name)) {
            val builder = KeyValueBucketConfiguration.Builder(name)
                .apply(block)
                .build()

            builder.applyTo(this)
        }

        return KeyValueBucket(this, name, KeyValueBucketInfo(stream.info))
    }
}