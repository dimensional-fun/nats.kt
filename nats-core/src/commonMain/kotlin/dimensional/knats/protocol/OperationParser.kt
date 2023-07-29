package dimensional.knats.protocol

import io.ktor.utils.io.*

/**
 *
 */
public interface OperationParser {
    public companion object;

    /**
     *
     */
    public suspend fun parse(channel: ByteReadChannel): Operation
}
