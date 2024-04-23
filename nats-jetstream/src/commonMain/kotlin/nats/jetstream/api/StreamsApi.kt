package nats.jetstream.api

import nats.core.protocol.Subject
import nats.core.protocol.json
import nats.jetstream.protocol.*
import nats.jetstream.protocol.domain.StreamConfig
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

@JvmInline
public value class StreamsApi(public val root: JetStreamApi) {
    public val subject: Subject get() = root.subject + "STREAM"

    public suspend fun create(request: StreamCreateRequest): StreamCreateResponse =
        root.want(root.request(subject + "CREATE" + request.config.name) { json(request) })

    public suspend fun list(request: StreamsRequest): StreamListResponse =
        root.want(root.request(subject + "LIST") { json(request) })

    public suspend fun names(request: StreamsRequest): StreamNamesResponse =
        root.want(root.request(subject + "NAMES") { json(request) })

    public suspend fun info(name: String, request: StreamInfoRequest): StreamInfoResponse =
        root.want(root.request(subject + "INFO" + name) { json(request) })

    public suspend fun delete(name: String): StreamDeleteResponse =
        root.want(root.request(subject + "DELETE" + name))

    public suspend fun getMessage(name: String, request: StreamMessageGetRequest): StreamMessageGetResponse =
        root.want(root.request(subject + "MSG.GET" + name) { json(request) })
}

@OptIn(ExperimentalContracts::class)
public suspend inline fun StreamsApi.create(
    name: String,
    block: StreamConfig.Builder.() -> Unit = {},
): StreamCreateResponse {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return create(StreamCreateRequest(StreamConfig.Builder(name).apply(block).build()))
}

@OptIn(ExperimentalContracts::class)
public suspend inline fun StreamsApi.info(
    name: String,
    block: StreamInfoRequest.Builder.() -> Unit = {},
): StreamInfoResponse {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return info(name, StreamInfoRequest.Builder().apply(block).build())
}

@OptIn(ExperimentalContracts::class)
public suspend inline fun StreamsApi.list(block: StreamsRequest.Builder.() -> Unit = {}): StreamListResponse {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return list(StreamsRequest.Builder().apply(block).build())
}
