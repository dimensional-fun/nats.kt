package dimensional.knats.transport

import io.ktor.network.sockets.*

internal expect suspend fun upgradeTlsNative(connection: Connection): TcpTransport