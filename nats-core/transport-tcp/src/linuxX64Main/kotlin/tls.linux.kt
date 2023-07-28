package dimensional.knats.connection.transport

import io.ktor.network.sockets.*
import io.ktor.network.tls.*
import kotlin.coroutines.coroutineContext

internal actual suspend fun upgradeTlsNative(connection: Connection): TcpTransport =
    TcpTransport(connection.tls(coroutineContext).connection())