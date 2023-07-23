package dimensional.knats.connection

import dimensional.knats.protocol.Operation

/**
 * An exception that was sent over the wire using an [Operation.Err] operation.
 */
public class NatsProtocolException(message: String) : Exception(message) {
    public constructor(op: Operation.Err) : this(op.message)
}
