package lib

import kotlinx.datetime.Instant
import kotlin.time.Duration

public open class Sample(
    public var start: Instant = Instant.fromEpochMilliseconds(0),
    public var end: Instant = Instant.fromEpochMilliseconds(0),
    public var jobMsgCount: Int = 0,
    public var msgCount: Long = 0,
    public var msgBytes: Long = 0,
    public var ioBytes: Long = 0,
) {
    public companion object {
        public const val BILLION: Double = 1000000000.0
    }

    public val duration: Duration get() = end - start

    public val throughput: Double get() = msgBytes / (duration.inWholeNanoseconds / BILLION)

    public val rate: Long get() = jobMsgCount / duration.inWholeSeconds

    override fun toString(): String = "$rate msgs/sec ~ $throughput/sec"
}