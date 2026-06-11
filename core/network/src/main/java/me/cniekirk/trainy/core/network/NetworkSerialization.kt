package me.cniekirk.trainy.core.network

import kotlinx.serialization.json.Json

object NetworkSerialization {
    val json: Json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
}
