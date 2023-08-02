package dimensional.knats.connection

import dimensional.knats.NatsServer
import dimensional.knats.NatsServerAddress
import dimensional.knats.client.ClientResources
import dimensional.knats.protocol.NatsConnectOptions
import dimensional.knats.protocol.NatsInfoOptions
import dimensional.knats.protocol.Operation
import dimensional.knats.transport.Transport
import dimensional.knats.transport.expect
import dimensional.knats.transport.readOperation
import dimensional.knats.transport.write
import kotlinx.coroutines.*
import naibu.ext.into
import naibu.monads.*
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
    public suspend fun connect(): Result<Pair<Transport, NatsServer>, Throwable> {
        var lastError: Throwable = IllegalStateException("No Servers Available")
        for (server in servers) {
            val (ts, info) = when (val result = connect(server)) {
                is Err -> {
                    lastError = result.value
                    continue
                }

                is Ok -> result.value
            }

            return Ok(ts to NatsServer(server, info))
        }

        return lastError.err()
    }

    /**
     *
     */
    public suspend fun connect(server: NatsServerAddress): Result<Pair<Transport, NatsInfoOptions>, Throwable> =
        Result {
            var ts = resources.transportFactory.connect(server, scope.coroutineContext)

            /* expect an INFO operation. */
            val info = ts.expect<Operation.Info>(resources.parser)

            /* check if we need to upgrade to TLS */
            val tlsEnabled = info.options.tlsRequired || info.options.tlsAvailable
            if (tlsEnabled) ts = ts.upgradeTLS()

            /* write Connect & Ping operations. */
            val connectOptions = NatsConnectOptions(
                verbose = false,
                headers = true.some(),
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

            ts to info.options
        }.into()
}