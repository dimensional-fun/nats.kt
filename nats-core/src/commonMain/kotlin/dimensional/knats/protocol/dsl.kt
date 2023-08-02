package dimensional.knats.protocol

import io.ktor.http.*
import naibu.ext.intoOrNull
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public fun Publication(
    subject: String,
    body: PublicationBody,
    replyTo: String? = null,
    headers: Headers? = null,
): Publication =
    if (headers == null) {
        Operation.Pub(subject, replyTo, body)
    } else {
        Operation.PubWithHeaders(subject, replyTo, headers, body)
    }

internal fun Publication.withReplyTo(replyTo: String): Publication = when (this) {
    is Operation.Pub -> copy(replyTo = replyTo)
    is Operation.PubWithHeaders -> copy(replyTo = replyTo)
}

/**
 *
 */
@OptIn(ExperimentalContracts::class)
public inline fun Publication(subject: String, block: PublicationBuilder.() -> Unit): Publication {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return PublicationBuilder(subject)
        .apply(block)
        .build()
}

/**
 * Convert this [Publication] into a [PublicationBuilder].
 */
public fun Publication.toBuilder(): PublicationBuilder {
    val builder = PublicationBuilder(subject)
    builder.replyTo = replyTo
    builder.body = body
    headers?.let(builder.headers::appendAll)

    return builder
}

/**
 * Whether this [Delivery] is to signify no-responders.
 */
public val Delivery.isNoResponders: Boolean
    get() = intoOrNull<Operation.MsgWithHeaders>()?.header?.contains("503") ?: false
