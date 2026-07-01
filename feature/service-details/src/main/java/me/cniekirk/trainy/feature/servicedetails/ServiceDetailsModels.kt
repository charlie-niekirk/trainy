package me.cniekirk.trainy.feature.servicedetails

import me.cniekirk.trainy.core.data.TrainServiceDetails

data class ServiceDetailsUiState(
    val details: TrainServiceDetails? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)
