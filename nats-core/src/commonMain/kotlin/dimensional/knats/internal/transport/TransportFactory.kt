package dimensional.knats.internal.transport

import dimensional.knats.protocol.NatsServerAddress
import kotlin.coroutines.CoroutineContext

public interface TransportFactory {
    public suspend fun connect(address: NatsServerAddress, context: CoroutineContext): Transport
}