package me.cniekirk.trainy.feature.search

import dev.zacsweers.metro.Inject

@Inject
class BuildDepartureBoardSearch(private val formatter: SearchDateTimeFormatter) {
    operator fun invoke(state: SearchUiState): BuildDepartureBoardSearchResult {
        val targetStation = state.targetStation.trim()
        val filterStation = state.filterStation.trim().ifEmpty { null }
        val dateTimeMillis = formatter.parse(state.date, state.time)

        return when {
            targetStation.isEmpty() -> {
                BuildDepartureBoardSearchResult.Error(
                    targetStationError = SearchValidationError.BlankStation
                )
            }

            dateTimeMillis == null -> {
                BuildDepartureBoardSearchResult.Error(
                    dateTimeError = SearchValidationError.InvalidDateTime
                )
            }

            else -> {
                BuildDepartureBoardSearchResult.Success(
                    DepartureBoardSearch(
                        mode = state.mode,
                        targetStation = targetStation,
                        filterStation = filterStation,
                        dateTimeMillis = dateTimeMillis,
                    )
                )
            }
        }
    }
}
