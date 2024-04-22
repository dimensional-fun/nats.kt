package nats.core.transport

import nats.core.NatsServerAddress
import kotlin.coroutines.CoroutineContext

public interface TransportFactory {
    public suspend fun connect(address: NatsServerAddress, context: CoroutineContext): Transport
}