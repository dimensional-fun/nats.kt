package dimensional.knats.client

import dimensional.knats.protocol.Delivery

public interface Dispatcher {
    public  fun start(block: suspend (Delivery) -> Unit)

    public suspend fun subscribe(subject: String)
}