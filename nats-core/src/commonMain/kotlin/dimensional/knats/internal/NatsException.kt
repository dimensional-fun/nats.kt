package dimensional.knats.internal

import dimensional.knats.protocol.Operation

/**
 *
 */
public sealed class NatsException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    public companion object {
        internal val PERMISSIONS_VIOLATION_REGEX = """(publish|subscription) to (\S+)""".toRegex(RegexOption.IGNORE_CASE)

        // Ported from nats.deno
        // https://github.com/nats-io/nats.deno/blob/main/nats-base-client/protocol.ts#L663
        public fun fromErr(value: Operation.Err): NatsException {
            val message = value.message.lowercase()
            return when {
                "permissions violation" in message -> {
                    val context = PERMISSIONS_VIOLATION_REGEX.find(value.message)?.let {
                        PermissionsViolation.Context(it.groups[0]!!.value, it.groups[1]!!.value)
                    }

                    PermissionsViolation(message, context)
                }

                "authorization violation" in message -> AuthorizationViolation(message)

                "authentication expired" in message -> AuthenticationExpired(message)

                else -> ProtocolException(message)
            }
        }
    }

    public class PermissionsViolation(message: String, public val context: Context?) : NatsException(message) {
        public data class Context(val operation: String, val subject: String)
    }

    public class AuthorizationViolation(message: String) : NatsException(message)

    public class AuthenticationExpired(message: String) : NatsException(message)


    /**
     * An exception that was sent over the wire using an [Operation.Err] operation.
     */
    public class ProtocolException(message: String) : NatsException(message)
}
