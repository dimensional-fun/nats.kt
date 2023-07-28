package dimensional.knats.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class NatsInfoOptions(
    @SerialName("server_id")
    val serverId: String,
    @SerialName("server_name")
    val serverName: String,
    val version: String,
    val go: String,
    val host: String,
    val port: Int,
    val headers: Boolean,
    @SerialName("max_payload")
    val maxPayload: Int,
    val proto: Int,
    @SerialName("auth_required")
    val authRequired: Int? = null,
    @SerialName("tls_required")
    val tlsRequired: Boolean = false,
    @SerialName("tls_version")
    val tlsVersion: Boolean? = null,
    @SerialName("tls_available")
    val tlsAvailable: Boolean = false,
    @SerialName("connect_urls")
    val connectUrls: List<String>? = null,
    @SerialName("ws_connect_urls")
    val wsConnectUrls: List<String>? = null,
    val ldm: Boolean? = null,
    @SerialName("git_commit")
    val gitCommit: String? = null,
    val jetstream: Boolean? = null,
    val ip: String? = null,
    @SerialName("client_id")
    val clientId: Int? = null,
    @SerialName("client_ip")
    val clientIp: String? = null,
    val nonce: String? = null,
    val cluster: String? = null,
    val domain: String? = null,
)
