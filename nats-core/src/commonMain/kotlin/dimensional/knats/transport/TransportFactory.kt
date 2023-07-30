package dimensional.knats.transport

import dimensional.knats.NatsServerAddress
import kotlin.coroutines.CoroutineContext

public interface TransportFactory {
    public suspend fun connect(address: NatsServerAddress, context: CoroutineContext): Transport
}