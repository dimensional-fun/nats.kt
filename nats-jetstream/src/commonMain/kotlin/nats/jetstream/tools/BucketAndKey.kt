package nats.jetstream.tools

public data class BucketAndKey(val bucket: String, val key: String) {
    public companion object {
        public operator fun invoke(value: String): BucketAndKey {
            val (_, b, k) = value.split('.', limit = 3)
            return BucketAndKey(b, k)
        }
    }
}
