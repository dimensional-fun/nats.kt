// DO NOT EDIT THIS FILE! This was generated by the `./gradlew :generateJetStreamClasses` task.`
package nats.jetstream.protocol

import kotlin.Int
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("io.nats.jetstream.api.v1.consumer_names_request")
public data class ConsumerNamesRequest(
    public val offset: Int,
)
