package nats.jetstream.protocol.domain

import kotlinx.serialization.Serializable

@Serializable
public enum class KeyValueOperation(public val code: String) {
    Delete("DEL"),
    Purge("PURGE"),
    Put("PUT");

    public companion object {
        public fun find(code: String): KeyValueOperation? = entries.find { it.code == code }
    }
}