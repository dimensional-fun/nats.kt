package nats.jetstream.client

import nats.core.client.Client
import nats.jetstream.client.impl.JetStreamClientImpl

/**
 */
public val Client.jetstream: JetStreamClient
    get() = JetStreamClientImpl(this)
