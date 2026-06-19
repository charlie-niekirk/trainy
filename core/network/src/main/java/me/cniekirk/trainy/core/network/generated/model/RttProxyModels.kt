package me.cniekirk.trainy.core.network.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class ClientTokenResponse(
    val token: String,
    val tokenType: TokenType,
    val expiresAt: String,
) {
    @Serializable
    enum class TokenType {
        @SerialName("Bearer") BEARER
    }
}

@Serializable
data class Stop(
    val namespace: String,
    val description: String,
    val shortCode: String? = null,
    val longCode: String? = null,
    val uniqueIdentity: String? = null,
)

@Serializable data class StationSummary(val crsCode: String, val name: String)

@Serializable data class StopsResponse(val data: List<Stop>, val meta: CollectionMeta)

@Serializable data class StationsResponse(val data: List<StationSummary>, val meta: CollectionMeta)

@Serializable data class CollectionMeta(val cacheStatus: CacheStatus, val count: Int)

@Serializable data class BoardResponse(val data: Map<String, JsonElement>?, val meta: ResponseMeta)

@Serializable data class ServiceResponse(val data: Map<String, JsonElement>, val meta: ResponseMeta)

@Serializable data class MetaResponse(val data: MetaResponseData, val meta: ResponseMeta)

@Serializable
data class MetaResponseData(
    val apiVersion: String,
    val rttApiVersion: String,
    val rttApiInfo: JsonObject? = null,
)

@Serializable data class ResponseMeta(val cacheStatus: CacheStatus)

@Serializable
enum class CacheStatus {
    HIT,
    MISS,
    STALE,
}

@Serializable
data class HealthResponse(val status: Status) {
    @Serializable
    enum class Status {
        @SerialName("ok") OK,
        @SerialName("unready") UNREADY,
    }
}
