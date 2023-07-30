package dimensional.knats.transport

import dimensional.knats.protocol.Operation
import dimensional.knats.protocol.OperationParser
import dimensional.knats.protocol.DefaultOperationParser
import naibu.logging.logging

internal val log by logging("dimensional.knats.wire")

internal suspend fun Transport.write(operation: Operation) {
    log.debug { "<<< $operation" }
    write(operation::write)
}

internal suspend fun Transport.readOperation(parser: OperationParser = DefaultOperationParser): Operation {
    incoming.awaitContent()

    val operation = parser.parse(incoming)
    log.debug { ">>> $operation" }

    return operation
}

internal suspend inline fun <reified T : Operation> Transport.expect(parser: OperationParser = DefaultOperationParser): T =
    when (val op = readOperation(parser)) {
        is T -> op
        else -> error("Expected ${T::class.simpleName}, received ${op.tag} instead.")
    }
