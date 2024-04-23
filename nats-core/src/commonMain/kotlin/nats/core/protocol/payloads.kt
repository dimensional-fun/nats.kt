package nats.core.protocol

import nats.core.tools.Json
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.serializer
import naibu.serialization.DefaultFormats
import naibu.text.charset.Charset
import naibu.text.charset.Charsets
import naibu.text.charset.encodeIntoByteArray

/**
 *
 */
public inline fun PublicationBuilder.payload(
    size: Long,
    contentType: ContentType = ContentType.Application.OctetStream,
    noinline block: suspend (ByteWriteChannel) -> Unit,
) {
    body = PublicationBody.Callback(size, block)
    contentType(contentType)
}

/**
 *
 */
public inline fun <reified T : Any> PublicationBuilder.payload(
    value: T,
    format: BinaryFormat,
    contentType: ContentType,
): Unit = payload(value, format.serializersModule.serializer(), format, contentType)

/**
 *
 */
public inline fun <T> PublicationBuilder.payload(
    value: T,
    serializer: SerializationStrategy<T>,
    format: BinaryFormat,
    contentType: ContentType,
) {
    body = PublicationBody.Packet(format.encodeToByteArray(serializer, value))
    contentType(contentType)
}

/**
 *
 */
public inline fun <reified T : Any> PublicationBuilder.payload(
    value: T,
    format: StringFormat,
    contentType: ContentType,
): Unit = payload(value, format.serializersModule.serializer(), format, contentType)

/**
 *
 */
public inline fun <T> PublicationBuilder.payload(
    value: T,
    serializer: SerializationStrategy<T>,
    format: StringFormat,
    contentType: ContentType,
    charset: Charset = Charsets.UTF_8,
): Unit = payload(format.encodeToString(serializer, value), charset = charset, contentType = contentType)

/**
 *
 */
public inline fun PublicationBuilder.payload(
    value: ByteReadChannel,
    size: Long,
    contentType: ContentType = ContentType.Application.OctetStream,
) {
    body = PublicationBody.ReadChannel(value, size)
    contentType(contentType)
}

/**
 *
 */
public inline fun PublicationBuilder.payload(
    value: ByteReadPacket,
    contentType: ContentType = ContentType.Application.OctetStream,
) {
    body = PublicationBody.Packet(value)
    contentType(contentType)
}

/**
 *
 */
public inline fun PublicationBuilder.payload(
    value: ByteArray,
    contentType: ContentType = ContentType.Application.OctetStream,
) {
    body = PublicationBody.Packet(value)
    contentType(contentType)
}

/**
 *
 */
public inline fun PublicationBuilder.payload(
    value: String,
    range: IntRange = value.indices,
    charset: Charset = Charsets.UTF_8,
    contentType: ContentType = ContentType.Text.Plain,
) {
    body = PublicationBody.Packet(value.encodeIntoByteArray(range, charset))
    contentType(contentType.withParameter("charset", charset.name))
}

public inline fun <reified T : Any> PublicationBuilder.json(
    value: T,
    format: StringFormat = DefaultFormats.Json,
): Unit = payload(value, format, ContentType.Application.Json)
