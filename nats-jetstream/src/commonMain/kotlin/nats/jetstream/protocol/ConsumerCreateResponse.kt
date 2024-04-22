package nats.jetstream.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nats.jetstream.protocol.domain.ConsumerInfo
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
@SerialName("io.nats.jetstream.api.v1.consumer_create_response")
public value class ConsumerCreateResponse(public val info: ConsumerInfo) : Response
