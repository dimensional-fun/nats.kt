package dimensional.knats.protocol

import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public fun Publication(
    subject: String,
    body: PublicationBody,
    replyTo: String? = null
): Publication =
    Operation.Pub(subject, replyTo, body)

public fun Publication(
    subject: String,
    body: PublicationBody,
    headers: Headers,
    replyTo: String? = null
): Publication =
    Operation.PubWithHeaders(subject, replyTo, headers, body)

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
