package me.cniekirk.trainy.feature.favourites

data class TrackedUiState(
    val services: List<TrackedServiceUiModel> = emptyList(),
    val isLoading: Boolean = true,
)

data class TrackedServiceUiModel(
    val serviceId: String,
    val time: String,
    val destination: String,
    val platform: String?,
    val isPlatformConfirmed: Boolean,
    val operatorName: String,
)
