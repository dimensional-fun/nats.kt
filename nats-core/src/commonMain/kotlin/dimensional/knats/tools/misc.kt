package dimensional.knats.tools

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

public fun String.escape(): String = replace("\n", "\\n").replace("\r", "\\r")

public val LongRange.size: Long get() = (endInclusive - first + 1).coerceAtLeast(0L)

public operator fun LongRange.plus(other: Long): LongRange = (first + other)..(last + other)

public fun CoroutineScope.child(
    context: CoroutineContext = EmptyCoroutineContext,
    supervisor: Boolean = true
): CoroutineScope {
    val job = if (supervisor) SupervisorJob(coroutineContext.job) else Job(coroutineContext.job)
    return CoroutineScope(coroutineContext + job + context)
}
