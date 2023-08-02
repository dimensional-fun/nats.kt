package dimensional.knats.client

import dimensional.knats.protocol.Delivery
import dimensional.knats.protocol.Publication
import dimensional.knats.protocol.PublicationBuilder
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 *
 */
public fun Client(resources: ClientResources): Client = ClientImpl(resources)

@OptIn(ExperimentalContracts::class)
public suspend inline fun Client(uri: String, block: ClientResourcesBuilder.() -> Unit): Client {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    /* build client resources */
    val resources = ClientResourcesBuilder(uri)
        .apply(block)
        .build()

    /* create the client & connect. */
    val client = Client(resources)
    client.connect()

    return client
}

/**
 *
 */
public suspend inline fun Client.publish(subject: String, block: PublicationBuilder.() -> Unit): Unit =
    publish(Publication(subject, block))

/**
 *
 */
public suspend inline fun Client.request(subject: String, block: PublicationBuilder.() -> Unit = {}): Delivery =
    request(Publication(subject, block))
