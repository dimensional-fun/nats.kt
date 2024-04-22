package nats.core.connection

import nats.core.NatsServer
import nats.core.NatsServerAddress
import nats.core.client.ClientResources
import nats.core.protocol.NatsConnectOptions
import nats.core.protocol.NatsInfoOptions
import nats.core.protocol.Operation
import nats.core.transport.Transport
import nats.core.transport.expect
import nats.core.transport.readOperation
import nats.core.transport.write
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

public data class Connector(val resources: ClientResources) {
    private val attempts = resources.servers.associateWith { 0 }.toMutableMap()

    /**
     *
     */
    public val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName("Connector"))

    /**
     *
     */
    public val servers: List<NatsServerAddress>
        get() = attempts.entries.mapNotNull { (server, reconnects) ->
            if (resources.maxReconnects == null || resources.maxReconnects < reconnects) server
            else null
        }

    /**
     *
     */
    public suspend fun connect(): Pair<Transport, NatsServer> {
        var lastError: Throwable = IllegalStateException("No Servers Available")
        for (server in servers) {
            val (ts, info) = try {
                connect(server)
            } catch (ex: Throwable) {
                lastError = ex
                continue
            }

            return ts to NatsServer(server, info)
        }

        throw lastError
    }

    /**
     *
     */
    public suspend fun connect(server: NatsServerAddress): Pair<Transport, NatsInfoOptions> {
        var ts = resources.transportFactory.connect(server, scope.coroutineContext)

        /* expect an INFO operation. */
        val info = ts.expect<Operation.Info>(resources.parser)

        /* check if we need to upgrade to TLS */
        val tlsEnabled = info.options.tlsRequired || info.options.tlsAvailable
        if (tlsEnabled) ts = ts.upgradeTLS()

        /* write Connect & Ping operations. */
        val connectOptions = NatsConnectOptions(
            verbose = false,
            headers = true,
            tlsRequired = tlsEnabled,
            noResponders = true
        )

        ts.write(Operation.Connect(connectOptions), Operation.Ping)
        ts.flush()

        /* wait for 'Pong' */
        while (coroutineContext.isActive) {
            when (val op = ts.readOperation(resources.parser)) {
                is Operation.Pong -> break
                is Operation.Ping -> ts.write(Operation.Pong)
                else -> error("Expected 'PING' or 'PONG' got $op")
            }
        }

        return ts to info.options
    }
}