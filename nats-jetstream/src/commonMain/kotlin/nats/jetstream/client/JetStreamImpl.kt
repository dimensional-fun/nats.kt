package nats.jetstream.client

import dimensional.knats.client.Client
import nats.jetstream.api.StreamsApi
//import nats.jetstream.protocol.StreamCreateRequest
import nats.jetstream.protocol.domain.StreamRetentionPolicy
import nats.jetstream.protocol.domain.StreamStorageType

public class JetStreamImpl(override val client: Client) : JetStream {
    override val streams: Streams = StreamsImpl(this)

    /**
     *
     */
    internal data class StreamImpl(override val name: String, override val js: JetStreamImpl) : Stream {

    }

    internal class StreamsImpl(override val js: JetStreamImpl) : Streams {
        override val api = StreamsApi(js)

        override fun get(name: String): Stream = StreamImpl(name, js)

        override suspend fun add(name: String, vararg subjects: String): Stream {
//            val request = StreamCreateRequest(
//                name = name,
//                subjects = subjects.toList(),
//                storage = StreamStorageType.File,
//                maxBytes = -1,
//                maxAge = 0,
//                retention = StreamRetentionPolicy.Interest,
//                maxConsumers = -1,
//                maxMsgs = -1,
//                numReplicas = 1
//            )
//
//            api.create(request)
            return get(name)
        }
    }

}
