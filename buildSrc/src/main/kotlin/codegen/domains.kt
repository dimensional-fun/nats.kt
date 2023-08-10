package codegen

import com.squareup.kotlinpoet.ClassName

const val proto = "nats.jetstream.protocol"

infix fun String.pk(other: String): ClassName = ClassName(proto, other)

val domains = mapOf(
    "retention" to d("StreamRetentionPolicy"),
    "storage" to d("StreamStorageType"),
    "ack_policy" to d("ConsumerAckPolicy"),
    "discard" to d("StreamDiscardType"),
    "replay_policy" to d("ConsumerReplayPolicy")
)

fun d(name: String) = ClassName("$proto.domain", name)
