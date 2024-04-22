package nats.jetstream.client.impl

import nats.jetstream.client.StreamsClient
import nats.jetstream.entity.behavior.StreamBehavior
import kotlin.jvm.JvmInline

@JvmInline
internal value class StreamsClientImpl(override val client: JetStreamClientImpl) : StreamsClient {
    override operator fun get(name: String): StreamBehavior = StreamBehavior(client, name)
}
