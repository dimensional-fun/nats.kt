@file:OptIn(ExperimentalForeignApi::class)

import dimensional.knats.NatsServerAddress
import dimensional.knats.client.Client
import dimensional.knats.client.request
import dimensional.knats.protocol.payload
import dimensional.knats.subscription.event.SubscriptionDeliveryEvent
import dimensional.knats.subscription.on
import dimensional.knats.transport.Transport
import dimensional.knats.transport.TransportFactory
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level
import io.ktor.utils.io.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import naibu.common.Closeable
import naibu.ext.print
import naibu.io.exception.IOException
import naibu.io.memory.DefaultAllocator
import naibu.io.memory.Memory
import naibu.monads.None
import naibu.monads.Option
import naibu.monads.some
import naibu.monads.unwrapOrElse
import platform.posix.*
import platform.posix.AF_INET
import platform.posix.IPPROTO_TCP
import platform.posix.WSAEINVAL
import platform.posix.WSAEPROTOTYPE
import platform.posix.WSAESHUTDOWN
import platform.posix.connect
import platform.windows.*
import platform.windows.SOCK_STREAM
import platform.windows.WSAGetLastError
import platform.windows.WSAStartup
import platform.windows.closesocket
import platform.windows.recv
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

public val NATS_ADDR: NatsServerAddress = NatsServerAddress("127.0.0.1", 4222)

public class SocketException(public val errno: Int) : IOException("Socket Exception: $errno")

@OptIn(ExperimentalForeignApi::class)
public value class Socket(public val handle: SOCKET) : Closeable {
    public fun read(buf: CValuesRef<ByteVar>?, amount: Int, flags: Int): Option<Int> {
        when (val result = recv(handle, buf, amount, flags)) {
            SOCKET_ERROR -> {
                val error = WSAGetLastError()
                if (error == WSAESHUTDOWN) return None
                throw SocketException(error)
            }

            else -> return result.some()
        }
    }

    public fun write(buf: CValuesRef<ByteVar>, amount: Int): Int {
        return send(handle, buf, amount, 0)
    }

    override fun close() {
        closesocket(handle)
    }

    override fun toString(): String {
        return "Socket(handle=$handle)"
    }

    public sealed class Address {
        public abstract val host: String

        public abstract val port: Int

        public data class V4(override val host: String, override val port: Int) : Address()

        public data class V6(override val host: String, override val port: Int) : Address()
    }

    public companion object {
        public val Empty: Socket = Socket(INVALID_SOCKET)

        public const val WSA_FLAG_NO_HANDLE_INHERIT: Int = 0x80

        public fun create(address: Address, type: Int): Socket {
            val family = address.toSocketFamily()

            var socket = createRawSocket(family, type, WSA_FLAG_OVERLAPPED or WSA_FLAG_NO_HANDLE_INHERIT)
            if (socket != INVALID_SOCKET) {
                return Socket(socket)
            }

            val error = WSAGetLastError()
            if (error != WSAEPROTOTYPE && error != WSAEINVAL) {
                error("Unable to create socket: $error")
            }

            socket = createRawSocket(family, type, WSA_FLAG_OVERLAPPED)
            if (socket == INVALID_SOCKET) {
                error("idk: ${WSAGetLastError()}")
            }

            return Socket(socket)
        }

        private fun createRawSocket(family: Int, type: Int, flags: Int): SOCKET {
            return WSASocketW(family, type, 0, null, 0.convert(), flags.convert())
        }

    }
}

@OptIn(ExperimentalForeignApi::class)
public class WindowsTcpTransport(private var socket: Socket, parent: CoroutineContext = EmptyCoroutineContext) :
    Transport {
    override val isClosed: Boolean get() = socket == Socket.Empty

    override val coroutineContext: CoroutineContext = parent + Dispatchers.IO + Job(parent.job)

    private val outgoing = reader(Dispatchers.Unconfined, channel = ByteChannel(false)) {
        val memory = DefaultAllocator.allocate(2048)
        while (!channel.isClosedForWrite) {
            val read = channel.readAvailable(memory.pointer, 0, memory.size)
            require(read > 0) {
                "Failed to read from channel."
            }

            val iResult = socket.write(memory.pointer, read)
            if (iResult == SOCKET_ERROR) {
                close()
                error("Writing to socket failed: ${WSAGetLastError()}")
            }
        }
    }.channel

    override val incoming: ByteReadChannel = writer(Dispatchers.Unconfined, channel = ByteChannel(false)) {
        while (!channel.isClosedForWrite) {
            var close = false
            channel.write { memory, startIndex, endIndex ->
                val bufferStart = memory.pointer + startIndex
                val size = endIndex - startIndex
                val bytesRead = try {
                    val read = socket.read(bufferStart, size.convert(), 0)
                    read.unwrapOrElse { close = true; 0 }
                } catch (ex: SocketException) {
                    if (ex.errno == EAGAIN) return@write 0
                    cancel("Failed to read from socket", ex)
                    ex.errno
                }

                bytesRead
            }

            channel.flush()
            if (close) {
                channel.close()
                break
            }
        }

        channel.closedCause?.let { throw it }
    }.channel

    override suspend fun close() {
        require(!isClosed)
        socket.close()
        socket = Socket.Empty
    }

    override suspend fun upgradeTLS(): Transport {
        TODO("TLS not supported")
    }

    override suspend fun write(block: suspend (ByteWriteChannel) -> Unit) {
        "a".print()
        block(outgoing)
        "b".print()
    }

    override suspend fun flush() {
        outgoing.flush()
    }

    override fun toString(): String {
        return "TcpTransport::Windows(socket=$socket)"
    }


    public companion object : TransportFactory {
        override suspend fun connect(address: NatsServerAddress, context: CoroutineContext): Transport {
            val addr = address.toSocketAddress()
            var socket = Socket.create(addr, SOCK_STREAM)

            val addrInfo = addr.toSockAddr(SOCK_STREAM, IPPROTO_TCP)
                ?: error("Unable to resolve address: $addr")

            val iResult = connect(socket.handle, addrInfo.pointed.ai_addr, addrInfo.pointed.ai_addrlen.convert())
            if (iResult == SOCKET_ERROR) {
                closesocket(socket.handle)
                socket = Socket.Empty
            }

            freeaddrinfo(addrInfo)
            require(socket != Socket.Empty) {
                "Unable to connect to NATS server."
            }

            return WindowsTcpTransport(socket, context)
        }
    }
}

public fun makeWORD(x: Int, y: Int): Int =
    ((y) shl 8) or x

public fun main(): Unit = runBlocking {
    initializeWinSock()

    KotlinLoggingConfiguration.logLevel = Level.TRACE
    KotlinLoggingConfiguration.formatter = naibu.log.NaibuFormatter

    val client = try {
        Client("nats://127.0.0.1:4234") {
//            transport = WebSocketTransport.Factory(WinHttp)
            transport = WindowsTcpTransport
        }
    } catch (ex: Throwable) {
        ex.printStackTrace()
        exitProcess(1)
    }

    "xd".print()

    /* start greeting people :D */
    val greeter = client.subscribe("greet")
    greeter.on<SubscriptionDeliveryEvent> {
        reply("Hello, ${delivery.readText()}")
    }

    /* start sending greet requests. */
    while (true) {
        delay(500.milliseconds)
        measureTime { client.request("greet") { payload("Gino") } }.print()
    }
}

@OptIn(ExperimentalForeignApi::class)
public fun initializeWinSock(): Unit = memScoped {
    val wsaData = alloc<WSADATA>()
    val iResult = WSAStartup(makeWORD(2, 2).convert(), wsaData.ptr)
    require(iResult == 0) {
        "Failed to initialize WinSock: $iResult"
    }
}

@OptIn(ExperimentalForeignApi::class)
public inline fun CoroutineScope.readFromSocket(
    fd: SOCKET,
    block: (Memory, Int) -> Unit,
) {
    val buffer = DefaultAllocator.allocate(4096)
    while (isActive) {
        val read = recv(fd, buffer.pointer, buffer.size.convert(), 0)
        if (read > 0) {
            block(buffer, read)
            continue
        }

        // nothing was read
        if (read == 0) {
            // closed
        } else {
            cancel("Failed to read from socket: ${WSAGetLastError()}")
            return
        }

        break
    }

    cancel("socket was closed")
}

public fun NatsServerAddress.toSocketAddress(): Socket.Address = Socket.Address.V4(host, port)

public fun Socket.Address.toSocketFamily(): Int = when (this) {
    is Socket.Address.V4 -> AF_INET
    is Socket.Address.V6 -> AF_INET6
}

public fun Socket.Address.toSockAddr(type: Int, protocol: Int): CPointer<addrinfo>? = memScoped {
    val hints = cValue<addrinfo> {
        ai_family = toSocketFamily()
        ai_socktype = type
        ai_protocol = protocol
    }

    val result = alloc<CPointerVar<addrinfo>>()

    //
    val iResult = getaddrinfo(host, port.toString(), hints, result.ptr)
    require(iResult == 0) {
        "Unable to resolve address: $iResult"
    }

    result.value
}
