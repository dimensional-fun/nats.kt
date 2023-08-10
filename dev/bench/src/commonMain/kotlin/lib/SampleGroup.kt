package lib

import kotlin.math.pow
import kotlin.math.sqrt

public class SampleGroup() : Sample() {
    public val samples: MutableList<Sample> = mutableListOf()

    /** The minimum of the message rates. */
    public val minRate: Long
        get() = samples.minOfOrNull { it.rate } ?: 0

    /** The maximum of the message rates */
    public val maxRate: Long
        get() = samples.maxOfOrNull { it.rate } ?: 0

    /** The average of the message rates. */
    public val avgRate: Long
        get() = samples.fold(0L) { acc, s -> acc + s.rate } / samples.size

    /** The standard deviation of the message rates. */
    public val stdDev: Double
        get() {
            val avg = avgRate
            return sqrt(samples.fold(0.0) { sum, sam -> sum + pow(sam.rate - avg, 2) } / samples.size)
        }

    /** Whether this [SampleGroup] has any samples */
    public fun hasSamples(): Boolean = samples.isNotEmpty()

    public fun addSample(sample: Sample) {
        if (samples.size == 1) {
            start = sample.start
            end = sample.end
        }

        ioBytes += sample.ioBytes
        msgCount += sample.msgCount
        msgBytes += sample.msgBytes
        jobMsgCount += sample.jobMsgCount
        start = minOf(start, sample.start)
        end = maxOf(end, sample.end)
    }

    /**
     *
     */
    public fun format(): String = "min $minRate | avg $avgRate | max $maxRate | stddev $stdDev msgs"

    public companion object {
        public fun <T : Number> pow(t: T, n: Int): Double = t.toDouble().pow(n)
    }
}