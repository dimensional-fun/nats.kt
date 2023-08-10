package lib

import dimensional.knats.tools.NUID
import kotlinx.coroutines.channels.Channel

/**
 * Initializes a Benchmark. After creating a bench call addSubSample/addPubSample. When done
 * collecting samples, call endBenchmark.
 *
 * @param name   a descriptive name for this test run
 * @param runId  a unique id for this test run (typically a guid)
 */
public class Benchmark(
    public val name: String,
    public val runId: String = NUID.next(),
) : Sample() {
    public val pubs: SampleGroup = SampleGroup()
    public val subs: SampleGroup = SampleGroup()

    private val pubChannel = Channel<Sample>()
    private val subChannel = Channel<Sample>()

    public suspend fun addPubSample(sample: Sample) {
        pubChannel.send(sample)
    }

    public suspend fun addSubSample(sample: Sample) {
        subChannel.send(sample)
    }

    /**
     * Closes this benchmark and calculates totals and times.
     */
    public fun close() {
        while (true) {
            subs.addSample(subChannel.tryReceive().getOrNull() ?: break)
        }

        while (true) {
            pubs.addSample(pubChannel.tryReceive().getOrNull() ?: break)
        }

        if (subs.samples.isNotEmpty()) {
            start = subs.start
            end = subs.end
            if (pubs.samples.isNotEmpty()) {
                end = minOf(end, pubs.end)
            }
        } else {
            start = pubs.start
            end = pubs.end
        }

        msgBytes = pubs.msgBytes + subs.msgBytes
        ioBytes = pubs.ioBytes + subs.ioBytes
        msgCount = pubs.msgCount + subs.msgCount
        jobMsgCount = pubs.jobMsgCount + subs.jobMsgCount
    }

    /**
     * Creates the output report.
     *
     * @return the report as a String.
     */
    public fun report(): String = buildString {
        fun SampleGroup.appendStats(indent: String) {
            for ((i, stat) in samples.withIndex()) {
                appendLine("$indent[${i + 1}] $stat ($jobMsgCount msgs)")
            }

            appendLine("$indent ${format()}")
        }

        appendLine("$name stats: $this")
        if (pubs.hasSamples()) {
            var indent = " "
            if (subs.hasSamples()) {
                appendLine("${indent}Pub stats: $pubs")
                indent = "  "
            }

            if (pubs.hasSamples()) pubs.appendStats(indent)
        }

        if (subs.hasSamples()) {
            appendLine(" Sub stats: $subs")
            if (subs.hasSamples()) subs.appendStats("  ")
        }
    }

    /**
     * Returns a string containing the report as series of CSV lines.
     *
     * @return a string with multiple lines
     */
    public fun csv(): String = buildString {
        appendLine("$name stats: $this")
        appendLine("#RunID, ClientID, Test Msgs, MsgsPerSec, BytesPerSec, Total Msgs, Total Bytes, DurationSecs")
        append(csvLines(subs, "S"))
        append(csvLines(pubs, "P"))
    }

    public fun csvLines(grp: SampleGroup, prefix: String): String = buildString {
        for ((j, stat) in grp.samples.withIndex())
            appendLine("$runId,$prefix$j,${stat.jobMsgCount},${stat.rate},${stat.throughput},${stat.msgCount},${stat.msgBytes},${stat.duration.inWholeNanoseconds / 1000000000.0}")
    }
}