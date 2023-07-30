package dimensional.knats.client

import dimensional.knats.protocol.Publication
import dimensional.knats.protocol.PublicationBuilder
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
public suspend inline fun Client(uri: String, block: ClientBuilder.() -> Unit): Client {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val client = ClientBuilder(uri)
        .apply(block)
        .build()

    client.connect()
    return client
}

/**
 *
 */
public suspend inline fun Client.publish(subject: String, block: PublicationBuilder.() -> Unit): Unit =
    publish(Publication(subject, block))

