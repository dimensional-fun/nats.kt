package nats.jetstream.protocol

import kotlinx.serialization.Serializable
import nats.jetstream.protocol.domain.*
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
public value class StreamCreateRequest(public val config: StreamConfig)
