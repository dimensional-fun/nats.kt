package dimensional.knats.protocol

import io.ktor.http.*

public sealed interface Publication {
    public companion object;

    public val subject: String

    public val replyTo: String? get() = null

    public val headers: Headers? get() = null

    public val body: PublicationBody
}