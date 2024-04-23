package nats.jetstream.client

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

@JvmInline
public value class BucketStore(public val client: JetStreamClient) {
    /**
     * Get a [Bucket] instance for the given [bucket name][bucket] regardless of whether it exists.
     *
     * @param bucket The name of the bucket.
     */
    public operator fun get(bucket: String): Bucket = Bucket(this, bucket)

    @OptIn(ExperimentalContracts::class)
    public suspend inline fun create(name: String, crossinline block: BucketConfiguration.Builder.() -> Unit = {}): Bucket {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

        val stream = client.streams.create(Bucket.prefix(name)) {
            val builder = BucketConfiguration.Builder()
                .apply(block)
                .build()

            builder.applyTo(this)
        }

        TODO()
    }
}