package me.cniekirk.trainy.feature.favourites

import me.cniekirk.trainy.core.data.TrackedTrainService

data class TrackedUiState(
    val services: List<TrackedTrainService> = emptyList(),
    val isLoading: Boolean = true,
)
