package nats.jetstream.protocol.domain

import kotlinx.serialization.Serializable
import nats.core.protocol.optional.Optional

@Serializable
public data class StreamMirrorExternal(
    /**
     * The subject prefix that imports the other account/domain $JS.API.CONSUMER.> subjects
     */
    val api: String,
    /**
     * The delivery subject to use for the push consumer
     */
    val delivery: Optional<String> = Optional.Missing(),
)