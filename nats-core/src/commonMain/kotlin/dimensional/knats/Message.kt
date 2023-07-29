package dimensional.knats

import dimensional.knats.protocol.Delivery

/**
 *
 */
public data class Message(val id: Long, val subscription: Subscription, val delivery: Delivery)
