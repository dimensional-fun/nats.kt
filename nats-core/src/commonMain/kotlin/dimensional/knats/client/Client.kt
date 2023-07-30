package dimensional.knats.client

import dimensional.knats.connection.Connection
import dimensional.knats.annotations.InternalNatsApi
import dimensional.knats.protocol.Publication
import dimensional.knats.subscription.Subscription

/**
 * A NATS client.
 */
public interface Client {
    @InternalNatsApi
    public val connection: Connection

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
    public suspend fun subscribe(subject: String, queueGroup: String? = null): Subscription

    /**
     *
     */
    public suspend fun subscribe(id: String, subject: String, queueGroup: String? = null): Subscription

    /**
     *
     */
    public suspend fun publish(publication: Publication)
}