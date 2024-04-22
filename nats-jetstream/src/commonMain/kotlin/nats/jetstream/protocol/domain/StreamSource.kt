package nats.jetstream.protocol.domain

import kotlinx.serialization.Serializable
import nats.core.protocol.optional.Optional
import nats.jetstream.protocol.Error
import nats.jetstream.tools.DurationAsNanoseconds

@Serializable
public data class StreamSource(
    /**
     * The name of the Stream being replicated.
     */
    val name: String,
    /**
     * How many messages behind the mirror operation is.
     */
    val lag: ULong,
    /**
     * When last the mirror had activity. Value will be -1 when that has been no activity.
     */
    val active: Optional<DurationAsNanoseconds> = Optional.Missing(),
    /**
     * Configuration referencing a stream source in another account or JetStream domain.
     */
    val external: Optional<StreamMirrorExternal> = Optional.Missing(),
    /**
     * No Description.
     * 'Error information if the source is not available.'
     */
    val error: Optional<Error> = Optional.Missing(),
)
