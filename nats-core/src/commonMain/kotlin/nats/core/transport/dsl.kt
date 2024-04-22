package nats.core.transport

import nats.core.protocol.DefaultOperationParser
import nats.core.protocol.Operation
import nats.core.protocol.OperationParser
import naibu.logging.logging

internal val log by logging("nats.core.wire")

internal suspend fun Transport.write(vararg operations: Operation) = write {
    for (operation in operations) {
        log.trace { "<<< $operation" }
        operation.write(it)
    }
}

internal suspend fun Transport.readOperation(parser: OperationParser = DefaultOperationParser): Operation {
    incoming.awaitContent()

    val operation = parser.parse(incoming)
    log.trace { ">>> $operation" }

    return operation
}

internal suspend inline fun <reified T : Operation> Transport.expect(parser: OperationParser = DefaultOperationParser): T =
    when (val op = readOperation(parser)) {
        is T -> op
        else -> error("Expected ${T::class.simpleName}, received ${op.tag} instead.")
    }
