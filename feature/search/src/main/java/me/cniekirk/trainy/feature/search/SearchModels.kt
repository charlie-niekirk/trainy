package me.cniekirk.trainy.feature.search

import kotlinx.serialization.Serializable

@Serializable
enum class SearchMode {
    Departing,
    Arriving,
}

@Serializable
data class DepartureBoardSearch(
    val mode: SearchMode,
    val targetStation: String,
    val filterStation: String?,
    val dateTimeMillis: Long,
)

data class SearchDateTime(
    val date: String,
    val time: String,
)

data class SearchUiState(
    val mode: SearchMode = SearchMode.Departing,
    val targetStation: String = "",
    val targetStationName: String = "",
    val filterStation: String = "",
    val filterStationName: String = "",
    val date: String = "",
    val time: String = "",
    val targetStationError: SearchValidationError? = null,
    val dateTimeError: SearchValidationError? = null,
)

enum class SearchValidationError {
    BlankStation,
    InvalidDateTime,
}

internal sealed interface SearchAction {
    data class ModeSelected(val mode: SearchMode) : SearchAction

    data class SelectStation(val field: me.cniekirk.trainy.feature.stationsearch.StationField) :
        SearchAction

    data class DateChanged(val value: String) : SearchAction

    data class TimeChanged(val value: String) : SearchAction

    data object SearchClicked : SearchAction
}

sealed interface SearchSideEffect {
    data class NavigateToDepartureBoard(val route: DepartureBoardRoute) : SearchSideEffect
}
