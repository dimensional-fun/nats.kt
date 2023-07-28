package dimensional.knats.connection

import dimensional.knats.connection.transport.Transport
import dimensional.knats.protocol.Operation
import dimensional.knats.protocol.OperationParser
import dimensional.knats.protocol.impl.DefaultOperationParser
import naibu.logging.logging

internal val log by logging("dimensional.knats.connection.wire")

internal suspend fun Transport.write(operation: Operation) {
    log.debug { "<<< $operation" }
    write { operation.encode(this) }
}

internal suspend fun Transport.readOperation(parser: OperationParser = DefaultOperationParser): Operation? {
    incoming.awaitContent()

    val operation = parser.parse(incoming)
    if (operation != null) log.debug { ">>> $operation" }

    return operation
}

internal suspend inline fun <reified T : Operation> Transport.expect(parser: OperationParser = DefaultOperationParser): T =
    when (val op = readOperation(parser)) {
        is T -> op
        else -> error("Expected ${T::class.simpleName}, received ${op?.tag} instead.")
    }
