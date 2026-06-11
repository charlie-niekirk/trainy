package me.cniekirk.trainy.core.network.model

@JvmInline
value class RttProxyBearerToken(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Bearer token must not be blank." }
    }

    internal fun authorizationHeader(): String = "Bearer $value"
}
