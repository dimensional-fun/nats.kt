package nats.core.protocol

import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.StringFormat
import naibu.text.charset.Charset
import naibu.text.charset.Charsets
import naibu.text.charset.decodeIntoString

public sealed interface Delivery {
    /** The subject that this message was sent with. */
    public val subject: Subject

    /** The ID of the subscription which received this message. */
    public val sid: String

    /** The subject that a reply should be sent to. */
    public val replyTo: Subject?

    /** The [Headers] that were sent along with this message. */
    public val headers: Headers?

    /**
     * The payload of this message.
     */
    public fun getPayload(): ByteReadPacket?

    /**
     * Decode the payload of this message as a [String].
     *
     * @param charset The charset to use when decoding the payload.
     * @return The decoded payload, or `null` if the payload could not be decoded.
     */
    public fun readText(charset: Charset = charsetHint): String? =
        getPayload()?.readBytes()?.decodeIntoString(charset = charset)

    /**
     * Decode the payload of this message using the given [DeserializationStrategy] and [BinaryFormat].
     *
     * @param strategy The strategy to use to decode the payload.
     * @param format   The format to use to decode the payload.
     * @return The decoded payload, or `null` if the payload could not be decoded.
     */
    public fun <T : Any> read(
        strategy: DeserializationStrategy<T>,
        format: BinaryFormat
    ): T? = getPayload()?.readBytes()?.let { format.decodeFromByteArray(strategy, it) }

    /**
     * Decode the payload of this message using the given [DeserializationStrategy] and [StringFormat].
     *
     * @param strategy The strategy to use to decode the payload.
     * @param format   The format to use to decode the payload.
     * @param charset  The charset to use when decoding the payload.
     * @return The decoded payload, or `null` if the payload could not be decoded.
     */
    public fun <T : Any> read(
        strategy: DeserializationStrategy<T>,
        format: StringFormat,
        charset: Charset = charsetHint
    ): T? = readText(charset)?.let { format.decodeFromString(strategy, it) }

    public companion object {
        public val Delivery.contentType: ContentType?
            get() = headers
                ?.get(HttpHeaders.ContentType)
                ?.let(ContentType::parse)

        public val Delivery.charsetHint: Charset
            get() = contentType?.parameter("charset")
                ?.let(Charsets::named)
                ?: Charsets.UTF_8
    }
}