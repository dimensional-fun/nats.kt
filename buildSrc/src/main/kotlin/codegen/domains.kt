package codegen

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

const val proto = "nats.jetstream.protocol"

infix fun String.pk(other: String): ClassName = ClassName(this, other)

val domains = mapOf(
    "sources" to LIST.parameterizedBy(d("StreamMirror")),
    "external" to d("StreamMirrorExternal"),
    "placement" to d("StreamReplicaPlacement"),
    "republish" to d("StreamRepublishConfiguration"),
    "retention" to d("StreamRetentionPolicy"),
    "storage" to d("StreamStorageType"),
    "discard" to d("StreamDiscardType"),
    "mirror" to d("StreamMirrorExternal"),
    //
    "replay_policy" to d("ConsumerReplayPolicy"),
    "ack_policy" to d("ConsumerAckPolicy"),
    "replicas" to d("ConsumerReplicas"),
    //
    "error" to (proto pk "Error"),
    "client" to d("Client"),
)

val TypeName.simpleName: String?
    get() = when (this) {
        is ClassName -> simpleName
        is ParameterizedTypeName -> rawType.simpleName
        is TypeVariableName -> name
        else -> null
    }

fun d(name: String) = ClassName("$proto.domain", name)
