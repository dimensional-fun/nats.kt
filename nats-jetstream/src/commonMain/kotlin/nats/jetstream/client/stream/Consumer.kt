package nats.jetstream.client.stream

import nats.jetstream.client.JetStreamClient
import nats.jetstream.protocol.domain.ConsumerInfo

public class Consumer(
    override val client: JetStreamClient,
    override val name: String,
    override val streamName: String,
    public val data: ConsumerInfo,
) : ConsumerBehavior {
    override suspend fun resolve(): Consumer = this

    override suspend fun resolveOrNull(): Consumer = this

    override fun toString(): String = "Consumer(stream=$streamName, name=$name, data=$data, client=$client)"
}
