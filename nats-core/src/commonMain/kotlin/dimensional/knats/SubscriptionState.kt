package dimensional.knats

import kotlin.jvm.JvmInline

public interface SubscriptionState {
    @JvmInline
    public value class Cancelled(public val after: Int? = null) : SubscriptionState

    public data object Active : SubscriptionState

    public data object Detached : SubscriptionState
}
