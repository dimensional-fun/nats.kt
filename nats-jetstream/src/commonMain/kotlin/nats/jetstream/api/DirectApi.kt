package nats.jetstream.api

import nats.core.client.request
import nats.core.protocol.Subject
import nats.core.protocol.json
import nats.jetstream.entity.MessageInfo
import nats.jetstream.protocol.StreamMessageGetRequest
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public class DirectApi(public val root: JetStreamApi) {
    public val subject: Subject get() = root.subject + "DIRECT"

    public suspend fun get(stream: String, request: StreamMessageGetRequest): MessageInfo.Direct {
        var subject = subject + "GET" + stream
        request.lastBySubject.value?.let { subject += it }

        val response = root.core.request(subject) {
            if (!request.isLastBySubject()) json(request)
        }

        // TODO: check status

        return MessageInfo.Direct(response)
    }
}

@OptIn(ExperimentalContracts::class)
public suspend inline fun DirectApi.get(
    stream: String,
    block: StreamMessageGetRequest.Builder.() -> Unit = {},
): MessageInfo.Direct {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return get(stream, StreamMessageGetRequest.Builder().apply(block).build())
}