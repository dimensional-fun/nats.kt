package nats.jetstream.client.kv

import nats.core.protocol.Subject

@Suppress("MemberVisibilityCanBePrivate")
public class KeyValueBucket(
    override val kv: KeyValueClient,
    override val name: String,
    public val info: KeyValueBucketInfo
) : KeyValueBucketBehavior {
    override suspend fun resolve(): KeyValueBucket = this

    override suspend fun resolveOrNull(): KeyValueBucket = this


    override fun toString(): String = "KeyValueBucket(name=$name, info=$info, kv=$kv)"

    override fun equals(other: Any?): Boolean = when (other)  {
        is KeyValueBucketBehavior -> other.name == name
        else -> false
    }

    override fun hashCode(): Int = arrayOf(name).contentHashCode()

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
