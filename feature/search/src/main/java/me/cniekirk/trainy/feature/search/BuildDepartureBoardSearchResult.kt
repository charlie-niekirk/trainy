package me.cniekirk.trainy.feature.search

sealed interface BuildDepartureBoardSearchResult {
    data class Success(
        val search: DepartureBoardSearch,
    ) : BuildDepartureBoardSearchResult

    data class Error(
        val targetStationError: SearchValidationError? = null,
        val dateTimeError: SearchValidationError? = null,
    ) : BuildDepartureBoardSearchResult
}
