package com.example.trainy.core.datastore

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val lastUpdatedEpochMillis: Long = 0L,
)
