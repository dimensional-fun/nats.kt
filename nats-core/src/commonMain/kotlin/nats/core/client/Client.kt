package nats.core.client

import nats.core.annotations.InternalNatsApi
import nats.core.connection.Connection
import nats.core.protocol.Delivery
import nats.core.protocol.Publication
import nats.core.protocol.Subject
import nats.core.subscription.Subscription

/**
 * A NATS client.
 */
public interface Client {
    @InternalNatsApi
    public val connection: Connection

    /**
     *
     */
    public val resources: ClientResources

    /**
     * The subscriptions that have been created w/ this [Client].
     */
    public val subscriptions: Map<String, Subscription>

    /**
     *
     */
    public suspend fun connect()

    /**
     *
     */
    public suspend fun subscribe(subject: Subject, queueGroup: String? = null): Subscription

    /**
     *
     */
    public suspend fun subscribe(id: String, subject: Subject, queueGroup: String? = null): Subscription

    /**
     *
     */
    public suspend fun publish(publication: Publication)

    /**
     *
     */
    public suspend fun request(publication: Publication): Delivery
}