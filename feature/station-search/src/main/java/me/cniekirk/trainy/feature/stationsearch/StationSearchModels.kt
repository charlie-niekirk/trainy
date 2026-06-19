package me.cniekirk.trainy.feature.stationsearch

import kotlinx.serialization.Serializable
import me.cniekirk.trainy.core.data.Station

@Serializable
enum class StationField {
    Target,
    Filter,
}

@Serializable
data class StationSearchRoute(val field: StationField) : androidx.navigation3.runtime.NavKey

data class StationSelectionResult(
    val field: StationField,
    val station: Station,
)

data class StationSearchUiState(
    val query: String = "",
    val stations: List<Station> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)

internal sealed interface StationSearchAction {
    data class QueryChanged(val value: String) : StationSearchAction

    data class StationSelected(val station: Station) : StationSearchAction

    data object Retry : StationSearchAction
}

sealed interface StationSearchSideEffect {
    data class ReturnSelection(val result: StationSelectionResult) : StationSearchSideEffect
}
