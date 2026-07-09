package me.cniekirk.trainy.feature.servicedetails

import me.cniekirk.trainy.core.data.TrainServiceDetails
import me.cniekirk.trainy.feature.stationdetails.StationDetailsRoute

data class ServiceDetailsUiState(
    val details: TrainServiceDetails? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)

sealed interface ServiceDetailsSideEffect {
    data class NavigateToStation(val route: StationDetailsRoute) : ServiceDetailsSideEffect
}
