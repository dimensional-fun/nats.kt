package dimensional.knats.protocol

import dimensional.knats.tools.SPACE
import dimensional.knats.tools.ktor.writeCRLF
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
 * Operations sent to the server from the client (S2C)
 */
@Serializable
public sealed interface Operation {
    public val tag: String

    /**
     *
     */
    public fun encode(out: Output) {
        out.writeText(tag)
        encodeOptions(out)
        out.writeCRLF()
    }

    public fun encodeOptions(out: Output)

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

        override fun encodeOptions(out: Output) {
            val json = DefaultFormats.Json.encodeToString(NatsInfoOptions.serializer(), options)
            out.writeText(json)
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

        override fun encodeOptions(out: Output) {
            out.writeByte(SPACE)
            val json = DefaultFormats.Json.encodeToString(NatsConnectOptions.serializer(), options)
            out.writeText(json)
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
        override val subject: String,
        override val replyTo: String?,
        override val body: PublicationBody,
    ) : Operation, Publication {
        override val tag: String get() = "PUB"

        override fun encodeOptions(out: Output) {
            out.writeArgument(subject, Output::writeSubject)
            out.writeArgument(replyTo, Output::writeText)

            //
            out.writeArgument(body.size, Output::writeAsText)
            out.writeCRLF()
            body.write(out)
        }
    }

    public data class PubWithHeaders(
        override val subject: String,
        override val replyTo: String?,
        override val headers: Headers,
        override val body: PublicationBody,
    ) : Operation, Publication {
        override val tag: String get() = "HPUB"

        override fun encodeOptions(out: Output) {
            out.writeArgument(subject, Output::writeSubject)
            out.writeArgument(replyTo, Output::writeText)

            val headers = buildPacket {
                writeText("NATS/1.0")
                writeCRLF()

                for ((name, value) in headers.flattenEntries()) {
                    writeText("$name: $value")
                    writeCRLF()
                }

                writeCRLF()
            }

            out.writeArgument(headers.remaining, Output::writeAsText)
            out.writeArgument(headers.remaining + body.size, Output::writeAsText)

            //
            out.writeCRLF()
            out.writePacket(headers)
            body.write(out)
        }
    }

    /**
     * [`SUB`][Sub] initiates a subscription to a subject, optionally joining a
     * distributed queue group.
     *
     * - [Protocol Page](https://docs.nats.io/reference/reference-protocols/nats-protocol#sub)
     */
    public data class Sub(val subject: String, val queueGroup: String?, val sid: String) : Operation {
        override val tag: String get() = "SUB"

        override fun encodeOptions(out: Output) {
            out.writeArgument(subject, Output::writeSubject)
            out.writeArgument(queueGroup, Output::writeText)
            out.writeArgument(sid, Output::writeText)
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

        override fun encodeOptions(out: Output) {
            out.writeArgument(sid, Output::writeSubject)
            out.writeArgument(maxMessages, Output::writeAsText)
        }
    }

    /**
     * The [`MSG`][Msg] protocol message is used to deliver an application message to the client.
     */
    public data class Msg(
        override val subject: String,
        override val sid: String,
        override val replyTo: String?,
        private val packet: ByteReadPacket?
    ) : Delivery, Operation {
        override val tag: String get() = "MSG"

        override val headers: Headers? get() = null

        override fun getPayload(): ByteReadPacket? = packet?.copy()

        override fun encodeOptions(out: Output) {
            out.writeArgument(subject, Output::writeSubject)
            out.writeArgument(sid, Output::writeText)
            out.writeArgument(replyTo, Output::writeText)
            out.writeArgument(packet?.remaining ?: 0, Output::writeAsText)

            //
            out.writeCRLF()
            packet?.copy()?.let(out::writePacket)
        }

        public class Builder {
            public lateinit var subject: String
            public lateinit var sid: String
            public var replyTo: String? = null
            public var payload: ByteReadPacket? = null

            public fun build(): Msg = Msg(subject, sid, replyTo, payload)
        }
    }

    /**
     *
     */
    public data class MsgWithHeaders(
        override val subject: String,
        override val sid: String,
        override val replyTo: String?,
        override val headers: Headers,
        val version: String,
        private val packet: ByteReadPacket?
    ) : Delivery, Operation {
        override val tag: String get() = "HMSG"

        override fun getPayload(): ByteReadPacket? = packet?.copy()

        override fun encodeOptions(out: Output) {
            out.writeArgument(subject, Output::writeSubject)
            out.writeArgument(sid, Output::writeText)
            out.writeArgument(replyTo, Output::writeText)

            /* create headers packet */
            val headers = buildPacket {
                for ((name, value) in headers.flattenEntries()) {
                    writeText("$name: $value")
                    writeCRLF()
                }
            }

            out.writeArgument(headers.remaining, Output::writeAsText)

            /* create payload packet. */
            val payload = buildPacket {
                writePacket(headers)
                writeCRLF()
                packet?.copy()?.let(::writePacket)
            }

            out.writeArgument(payload.remaining, Output::writeAsText)

            /* write payload packet. */
            out.writeCRLF()
            out.writePacket(payload)
        }


        public class Builder {
            public lateinit var subject: String
            public lateinit var sid: String
            public lateinit var version: String
            public var replyTo: String? = null
            public var headers: HeadersBuilder = HeadersBuilder()
            public var payload: ByteReadPacket? = null

            internal var hdrLen by Delegates.notNull<Int>()
            internal var totLen by Delegates.notNull<Int>()

            public fun build(): MsgWithHeaders =
                MsgWithHeaders(subject, sid, replyTo, headers.build(), version, payload)
        }
    }

    /**
     * - [Protocol Page](https://docs.nats.io/reference/reference-protocols/nats-protocol#ping-pong)
     */
    public data object Ping : Operation {
        override val tag: String get() = "PING"
        override fun encodeOptions(out: Output): Unit = Unit
    }

    /**
     * - [Protocol Page](https://docs.nats.io/reference/reference-protocols/nats-protocol#ping-pong)
     */
    public data object Pong : Operation {
        override val tag: String get() = "PONG"
        override fun encodeOptions(out: Output): Unit = Unit
    }

    /**
     * When the verbose connection option is set to true (the default value), the server acknowledges
     * each well-formed protocol message from the client with an `+OK` message.
     *
     * - [Protocol Page](https://docs.nats.io/reference/reference-protocols/nats-protocol#+ok-err)
     */
    public data object Ok : Operation {
        override val tag: String get() = "+OK"
        override fun encodeOptions(out: Output): Unit = Unit
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

        override fun encodeOptions(out: Output) {
            out.writeArgument(message, Output::writeText)
        }
    }
}
