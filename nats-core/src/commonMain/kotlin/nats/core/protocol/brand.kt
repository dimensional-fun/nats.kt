package nats.core.protocol

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

/**
 * A NATS subject.
 */
@JvmInline
@Serializable(with = Subject.Serializer::class)
public value class Subject(public val value: String) {
    public operator fun plus(other: String): Subject = Subject("$value.$other")

    public operator fun plus(other: Subject): Subject = Subject("$value.${other.value}")

    override fun toString(): String = value

    public object Serializer : KSerializer<Subject> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("nats.core.protocol.Subject", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Subject) {
            encoder.encodeString(value.value)
        }

        override fun deserialize(decoder: Decoder): Subject {
            return Subject(decoder.decodeString())
        }
    }

    public companion object Validator {
        /**
         * Validate the given [subject] for the given [label].
         *
         * The [subject] may not contain spaces (e.g., \r \n \t), start or end with a token delimiter
         */
        public fun validate(
            subject: String,
            label: String = "Subject",
        ): String {
            var error: String? = null
            fun check() = error?.let { throw SubjectInvalidException("$label $it") }

            error = when {
                subject.isBlank() -> "cannot be blank."
                subject.startsWith('.') -> "cannot start with a token delimiter ('.')."
                subject.endsWith('.') -> "cannot end with a token delimiter ('.')."
                else -> null
            }
            check()

            val segments = subject.split("\\.")
            for ((i, segment) in segments.withIndex()) {
                check()
                if (segment.isBlank()) {
                    error = "segment cannot be empty or blank."
                    continue
                }

                for (char in segment) {
                    check()
                    error = when {
                        char.isWhitespace() ->
                            "$label segment cannot contain whitespace."
                        char == '*' && segment.length != 1 ->
                            "'*' wildcard must be the only character in a segment."
                        char == '>' && segment.length != 1 ->
                            "'>' wildcard must be the only character in a segment."
                        char == '>' && i == segments.lastIndex ->
                            "'>' wildcard must be the last segment."
                        else -> null
                    }
                }
            }

            return subject
        }
    }
}