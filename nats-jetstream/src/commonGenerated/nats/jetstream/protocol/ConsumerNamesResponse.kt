// DO NOT EDIT THIS FILE! This was generated by the `./gradlew :generateJetStreamClasses` task.`
package nats.jetstream.protocol

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("io.nats.jetstream.api.v1.consumer_names_response")
public data class ConsumerNamesResponse(
    public val consumers: List<String>,
) : Response
