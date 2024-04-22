package nats.jetstream.api

import nats.core.protocol.Subject
import nats.core.protocol.json
import nats.jetstream.protocol.*
import nats.jetstream.protocol.domain.*
import kotlin.jvm.JvmInline

@JvmInline
public value class ConsumerApi(public val root: JetStreamApi) {
    public val subject: Subject get() = root.subject + "CONSUMER"

    /**
     */
    public suspend fun create(request: ConsumerCreateRequest): ConsumerCreateResponse =
        root.want(root.request(subject + "CREATE" + request.streamName + request.config.name) { json(request) })

    /**
     * Get the names of all consumers for the given [stream name][stream]
     *
     * @param stream the name of the stream to get the consumer names for.
     * @param offset the offset to start at.
     */
    public suspend fun names(stream: String, offset: Int): ConsumerNamesResponse =
        root.want(root.request(subject + "NAMES" + stream) { json(OffsetRequest(offset)) })

    /**
     * List all consumers for the given [stream name][stream].
     * Unlike [names], this method returns the full [ConsumerInfo] for each consumer.
     *
     * @param stream the name of the stream to list consumers for.
     * @param offset the offset to start at.
     */
    public suspend fun list(stream: String, offset: Int): ConsumerListResponse =
        root.want(root.request(subject + "LIST" + stream) { json(OffsetRequest(offset)) })

    public suspend fun info(stream: String, name: String): ConsumerInfoResponse =
        root.want(root.request(subject + "INFO" + stream + name))

    /**
     * Delete a consumer by its [name].
     *
     * @param name the name of the consumer to delete.
     * @return the response from the server.
     */
    public suspend fun delete(name: String): ConsumerDeleteResponse =
        root.want(root.request(subject + "DELETE" + name))
}