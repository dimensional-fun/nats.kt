package nats.core.protocol

import io.ktor.http.*

public sealed interface Publication {
    public companion object;

    /**
     *
     */
    public val subject: Subject

    /**
     *
     */
    public val body: PublicationBody

    /**
     *
     */
    public val replyTo: Subject? get() = null

    /**
     *
     */
    public val headers: Headers? get() = null
}