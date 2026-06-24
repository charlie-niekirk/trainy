package me.cniekirk.trainy.feature.search.usecase

import dev.zacsweers.metro.Inject
import me.cniekirk.trainy.feature.search.SearchUiState
import me.cniekirk.trainy.feature.search.SearchValidationError
import me.cniekirk.trainy.feature.search.time.SearchDateTimeFormatter
import me.cniekirk.trainy.feature.servicelist.ServiceListSearch

@Inject
class BuildServiceListSearchUseCase(private val formatter: SearchDateTimeFormatter) {
    operator fun invoke(state: SearchUiState): BuildServiceListSearchResult {
        val targetStation = state.targetStation.trim()
        val filterStation = state.filterStation.trim().ifEmpty { null }
        val dateTimeMillis = formatter.parse(state.date, state.time)

        return when {
            targetStation.isEmpty() -> {
                BuildServiceListSearchResult.Error(
                    targetStationError = SearchValidationError.BlankStation
                )
            }

            dateTimeMillis == null -> {
                BuildServiceListSearchResult.Error(
                    dateTimeError = SearchValidationError.InvalidDateTime
                )
            }

            else -> {
                BuildServiceListSearchResult.Success(
                    ServiceListSearch(
                        mode = state.mode,
                        targetStation = targetStation,
                        targetStationName = state.targetStationName.trim(),
                        filterStation = filterStation,
                        filterStationName = state.filterStationName.trim(),
                        dateTimeMillis = dateTimeMillis,
                    )
                )
            }
        }
    }
}
