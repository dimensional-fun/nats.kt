package nats.jetstream.client

import nats.core.client.Client

/**
 */
public val Client.jetstream: JetStreamClient
    get() = JetStreamClient(this)
