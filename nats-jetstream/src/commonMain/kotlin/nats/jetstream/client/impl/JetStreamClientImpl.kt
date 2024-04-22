package nats.jetstream.client.impl

import nats.core.client.Client
import nats.jetstream.api.JetStreamApi
import nats.jetstream.client.JetStreamClient
import nats.jetstream.client.StreamsClient
//import nats.jetstream.protocol.StreamCreateRequest
import kotlin.jvm.JvmInline

@JvmInline
public value class JetStreamClientImpl(override val core: Client) : JetStreamClient {
    override val api: JetStreamApi get() = JetStreamApi(core)

    override val streams: StreamsClient get() = StreamsClientImpl(this)
}
