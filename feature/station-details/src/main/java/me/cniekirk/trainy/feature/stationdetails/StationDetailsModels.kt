package me.cniekirk.trainy.feature.stationdetails

import me.cniekirk.trainy.core.data.StationDetails

data class StationDetailsUiState(
    val details: StationDetails? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)
