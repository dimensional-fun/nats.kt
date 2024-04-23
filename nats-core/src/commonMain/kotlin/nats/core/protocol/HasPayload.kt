package nats.core.protocol

import io.ktor.utils.io.core.*
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.serializer
import naibu.text.charset.Charset
import naibu.text.charset.decodeIntoString

public interface HasPayload {
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
    public fun readText(charset: Charset): String? =
        getPayload()?.readBytes()?.decodeIntoString(charset = charset)

    /**
     * Decode the payload of this message using the given [DeserializationStrategy] and [BinaryFormat].
     *
     * @param strategy The strategy to use to decode the payload.
     * @param format   The format to use to decode the payload.
     * @return The decoded payload, or `null` if the payload could not be decoded.
     * @throws MissingPayloadException If this message doesn't have a payload.
     */
    public fun <T> read(
        strategy: DeserializationStrategy<T>,
        format: BinaryFormat
    ): T {
        val payload = getPayload()
                      ?: throw MissingPayloadException("Payload is missing.")

        return format.decodeFromByteArray(strategy, payload.readBytes())
    }

    /**
     * Decode the payload of this message using the given [DeserializationStrategy] and [StringFormat].
     *
     * @param strategy The strategy to use to decode the payload.
     * @param format   The format to use to decode the payload.
     * @param charset  The charset to use when decoding the payload.
     * @return The decoded payload, or `null` if the payload could not be decoded.
     * @throws MissingPayloadException If this message doesn't have a payload..
     */
    public fun <T> read(
        strategy: DeserializationStrategy<T>,
        format: StringFormat,
        charset: Charset
    ): T {
        val text = readText(charset)
                   ?: throw MissingPayloadException("Payload is missing.")

        return format.decodeFromString(strategy, text)
    }
}

public class MissingPayloadException(message: String) : Exception(message)

/**
 *
 */
public inline fun <reified T> HasPayload.read(format: StringFormat, charset: Charset): T =
    read(format.serializersModule.serializer(), format, charset)
