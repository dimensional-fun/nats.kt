package nats.core.client

import nats.core.protocol.Delivery
import nats.core.protocol.Publication
import nats.core.protocol.PublicationBuilder
import nats.core.protocol.Subject
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
public suspend inline fun Client.publish(subject: Subject, block: PublicationBuilder.() -> Unit): Unit =
    publish(Publication(subject, block))

/**
 *
 */
public suspend inline fun Client.request(subject: Subject, block: PublicationBuilder.() -> Unit = {}): Delivery =
    request(Publication(subject, block))
