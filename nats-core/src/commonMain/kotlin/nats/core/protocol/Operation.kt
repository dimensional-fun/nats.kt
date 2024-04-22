package nats.core.protocol

import nats.core.tools.SPACE
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.Serializable
import naibu.serialization.DefaultFormats
import naibu.serialization.json.Json
import kotlin.jvm.JvmInline
import kotlin.properties.Delegates

/**
 */
@Serializable
public sealed interface Operation {
    public val tag: String

    /**
     *
     */
    public suspend fun write(channel: ByteWriteChannel) {
        channel.writeStringUtf8(tag)
        writeInner(channel)
        channel.writeCRLF()
    }

    public suspend fun writeInner(channel: ByteWriteChannel)

    /**
     * A client will need to start as a plain TCP connection, then when the server accepts a connection from the client,
     * it will send information about itself, the configuration and security requirements necessary for the client to
     * successfully authenticate with the server and exchange messages.
     *
     * When using the updated client protocol (see [`CONNECT`][Connect] below), [`INFO`][Info] messages can be sent anytime by the server.
     * This means clients with that protocol level need to be able to asynchronously handle [`INFO`][Info] messages.
     *
     * - [Protocol Page](https://docs.nats.io/reference/reference-protocols/nats-protocol#info)
     */
    @JvmInline
    public value class Info(public val options: NatsInfoOptions) : Operation {
        override val tag: String get() = "INFO"

        override suspend fun writeInner(channel: ByteWriteChannel) {
            val json = DefaultFormats.Json.encodeToString(NatsInfoOptions.serializer(), options)
            channel.writeStringUtf8(json)
        }
    }

    /**
     * The [`CONNECT`][Connect] message is the client version of the [`CONNECT`][Info] message.
     *
     * Once the client has established a TCP/IP socket connection with the NATS server, and an [`INFO`][Info] message has
     * been received from the server, the client may send a [`CONNECT`][Connect] message to the NATS server to provide
     * more information about the current connection as well as security information.
     *
     * - [Protocol Page](https://docs.nats.io/reference/reference-protocols/nats-protocol#connect)
     */
    @JvmInline
    public value class Connect(public val options: NatsConnectOptions) : Operation {
        override val tag: String get() = "CONNECT"

        override suspend fun writeInner(channel: ByteWriteChannel) {
            channel.writeByte(SPACE)
            val json = DefaultFormats.Json.encodeToString(NatsConnectOptions.serializer(), options)
            channel.writeStringUtf8(json)
        }
    }

    /**
     * The [`PUB`][Pub] message publishes the message payload to the given subject name, optionally supplying a reply subject.
     *
     * If a reply subject is supplied, it will be delivered to eligible subscribers along with the supplied payload.
     * Note that the payload itself is optional.
     *
     * To omit the payload, set the payload size to 0, but the second CRLF is still required.
     *
     * - [Protocol Page](https://docs.nats.io/reference/reference-protocols/nats-protocol#pub)
     */
    public data class Pub(
        override val subject: Subject,
        override val replyTo: Subject?,
        override val body: PublicationBody,
    ) : Operation, Publication {
        override val tag: String get() = "PUB"

        override suspend fun writeInner(channel: ByteWriteChannel) {
            channel.writeArgument(subject, ByteWriteChannel::writeSubject)
            channel.writeArgument(replyTo, ByteWriteChannel::writeSubject)

            //
            channel.writeArgument(body.size, ByteWriteChannel::writeAsText)
            channel.writeCRLF()
            body.write(channel)
        }
    }

    public data class PubWithHeaders(
        override val subject: Subject,
        override val replyTo: Subject?,
        override val headers: Headers,
        override val body: PublicationBody,
    ) : Operation, Publication {
        override val tag: String get() = "HPUB"

        override suspend fun writeInner(channel: ByteWriteChannel) {
            channel.writeArgument(subject, ByteWriteChannel::writeSubject)
            channel.writeArgument(replyTo, ByteWriteChannel::writeSubject)

            val headers = buildPacket {
                writeText("NATS/1.0")
                writeCRLF()

                for ((name, value) in headers.flattenEntries()) {
                    writeText("$name: $value")
                    writeCRLF()
                }

                writeCRLF()
            }

            channel.writeArgument(headers.remaining, ByteWriteChannel::writeAsText)
            channel.writeArgument(headers.remaining + body.size, ByteWriteChannel::writeAsText)

            //
            channel.writeCRLF()
            channel.writePacket(headers)
            body.write(channel)
        }
    }

    /**
     * [`SUB`][Sub] initiates a subscription to a subject, optionally joining a
     * distributed queue group.
     *
     * - [Protocol Page](https://docs.nats.io/reference/reference-protocols/nats-protocol#sub)
     */
    public data class Sub(val subject: Subject, val queueGroup: String?, val sid: String) : Operation {
        override val tag: String get() = "SUB"

        override suspend fun writeInner(channel: ByteWriteChannel) {
            channel.writeArgument(subject, ByteWriteChannel::writeSubject)
            channel.writeArgument(queueGroup, ByteWriteChannel::writeStringUtf8)
            channel.writeArgument(sid, ByteWriteChannel::writeStringUtf8)
        }
    }

    /**
     * [`UNSUB`][Unsub] unsubscribes the connection from the specified subject, or auto-unsubscribes after the specified number
     * of messages has been received.
     *
     * - [Protocol Page](https://docs.nats.io/reference/reference-protocols/nats-protocol#unsub)
     */
    public data class Unsub(val sid: String, val maxMessages: Int?) : Operation {
        override val tag: String get() = "UNSUB"

        override suspend fun writeInner(channel: ByteWriteChannel) {
            channel.writeArgument(sid, ByteWriteChannel::writeASCII)
            channel.writeArgument(maxMessages, ByteWriteChannel::writeAsText)
        }
    }

    /**
     * The [`MSG`][Msg] protocol message is used to deliver an application message to the client.
     */
    public data class Msg(
        override val subject: Subject,
        override val sid: String,
        override val replyTo: Subject?,
        private val packet: ByteReadPacket?,
    ) : Delivery, Operation {
        override val tag: String get() = "MSG"

        override val headers: Headers? get() = null

        override fun getPayload(): ByteReadPacket? = packet?.copy()

        override suspend fun writeInner(channel: ByteWriteChannel) {
            channel.writeArgument(subject, ByteWriteChannel::writeSubject)
            channel.writeArgument(sid, ByteWriteChannel::writeStringUtf8)
            channel.writeArgument(replyTo, ByteWriteChannel::writeSubject)
            channel.writeArgument(packet?.remaining ?: 0, ByteWriteChannel::writeAsText)

            //
            channel.writeCRLF()
            packet?.copy()?.let { channel.writePacket(it) }
        }

        public class Builder {
            public lateinit var sid: String
            public var subject: Subject by Delegates.notNull()
            public var replyTo: Subject? = null
            public var payload: ByteReadPacket? = null

            public fun build(): Msg = Msg(subject, sid, replyTo, payload)
        }
    }

    /**
     *
     */
    public data class MsgWithHeaders(
        override val subject: Subject,
        override val sid: String,
        override val replyTo: Subject?,
        override val headers: Headers,
        val header: String,
        private val packet: ByteReadPacket?,
    ) : Delivery, Operation {
        override val tag: String get() = "HMSG"

        override fun getPayload(): ByteReadPacket? = packet?.copy()

        override suspend fun writeInner(channel: ByteWriteChannel) {
            channel.writeArgument(subject, ByteWriteChannel::writeSubject)
            channel.writeArgument(sid, ByteWriteChannel::writeStringUtf8)
            channel.writeArgument(replyTo, ByteWriteChannel::writeSubject)

            /* create headers packet */
            val headers = buildPacket {
                for ((name, value) in headers.flattenEntries()) {
                    writeText("$name: $value")
                    writeCRLF()
                }
            }

            channel.writeArgument(headers.remaining, ByteWriteChannel::writeAsText)

            /* create payload packet. */
            val payload = buildPacket {
                writePacket(headers)
                writeCRLF()
                packet?.copy()?.let(::writePacket)
            }

            channel.writeArgument(payload.remaining, ByteWriteChannel::writeAsText)

            /* write payload packet. */
            channel.writeCRLF()
            channel.writePacket(payload)
        }


        public class Builder {
            public lateinit var sid: String
            public lateinit var version: String
            public var subject: Subject by Delegates.notNull()
            public var replyTo: Subject? = null
            public var headers: HeadersBuilder = HeadersBuilder()
            public var payload: ByteReadPacket? = null

            internal var hdrLen: Int by Delegates.notNull()
            internal var totLen: Int by Delegates.notNull()

            public fun build(): MsgWithHeaders =
                MsgWithHeaders(subject, sid, replyTo, headers.build(), version, payload)
        }
    }

    /**
     * - [Protocol Page](https://docs.nats.io/reference/reference-protocols/nats-protocol#ping-pong)
     */
    public data object Ping : Operation {
        override val tag: String get() = "PING"
        override suspend fun writeInner(channel: ByteWriteChannel): Unit = Unit
    }

    /**
     * - [Protocol Page](https://docs.nats.io/reference/reference-protocols/nats-protocol#ping-pong)
     */
    public data object Pong : Operation {
        override val tag: String get() = "PONG"
        override suspend fun writeInner(channel: ByteWriteChannel): Unit = Unit
    }

    /**
     * When the verbose connection option is set to true (the default value), the server acknowledges
     * each well-formed protocol message from the client with an `+OK` message.
     *
     * - [Protocol Page](https://docs.nats.io/reference/reference-protocols/nats-protocol#+ok-err)
     */
    public data object Ok : Operation {
        override val tag: String get() = "+OK"
        override suspend fun writeInner(channel: ByteWriteChannel): Unit = Unit
    }

    /**
     * The `-ERR` message is used by the server indicate a protocol, authorization, or
     * other runtime connection error to the client. Most of these errors result in the
     * server closing the connection.
     *
     * - [Protocol Page](https://docs.nats.io/reference/reference-protocols/nats-protocol#+ok-err)
     */
    public data class Err(val message: String) : Operation {
        override val tag: String get() = "-ERR"

        override suspend fun writeInner(channel: ByteWriteChannel) {
            channel.writeArgument(message, ByteWriteChannel::writeStringUtf8)
        }
    }
}
