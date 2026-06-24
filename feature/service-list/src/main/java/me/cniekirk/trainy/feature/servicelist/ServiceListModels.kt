package me.cniekirk.trainy.feature.servicelist

import kotlinx.serialization.Serializable
import me.cniekirk.trainy.core.data.TrainService

@Serializable
enum class ServiceListMode {
    Departing,
    Arriving,
}

@Serializable
data class ServiceListSearch(
    val mode: ServiceListMode,
    val targetStation: String,
    val targetStationName: String = "",
    val filterStation: String?,
    val filterStationName: String = "",
    val dateTimeMillis: Long,
)

data class ServiceListUiState(
    val services: List<TrainService> = emptyList(),
    val trackedServiceIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)

sealed interface ServiceListSideEffect {
    data object ShowTrackingError : ServiceListSideEffect
}
