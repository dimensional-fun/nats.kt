package nats.jetstream.tools

import kotlinx.serialization.Serializable
import naibu.serialization.common.DurationSerializer
import kotlin.time.Duration
import kotlin.time.DurationUnit

public object DurationNanosecondSerializer : DurationSerializer(DurationUnit.NANOSECONDS)

public typealias DurationAsNanoseconds = @Serializable(with = DurationNanosecondSerializer::class) Duration
