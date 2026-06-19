package me.cniekirk.trainy.feature.search

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import me.cniekirk.trainy.feature.stationsearch.StationSelectionResult
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class SearchViewModel(
    createDefaultSearchDateTime: CreateDefaultSearchDateTime,
    private val buildDepartureBoardSearch: BuildDepartureBoardSearch,
) : ViewModel(), ContainerHost<SearchUiState, SearchSideEffect> {
    private val defaultDateTime = createDefaultSearchDateTime()

    override val container: Container<SearchUiState, SearchSideEffect> =
        container(
            initialState =
                SearchUiState(
                    date = defaultDateTime.date,
                    time = defaultDateTime.time,
                )
        )

    fun onModeSelected(mode: SearchMode) = intent {
        reduce {
            state.copy(
                mode = mode,
                targetStationError = null,
            )
        }
    }

    fun onTargetStationChanged(value: String) = intent {
        reduce {
            state.copy(
                targetStation = value,
                targetStationError = null,
            )
        }
    }

    fun onFilterStationChanged(value: String) = intent {
        reduce { state.copy(filterStation = value) }
    }

    fun onStationSelected(result: StationSelectionResult) = intent {
        reduce {
            when (result.field) {
                me.cniekirk.trainy.feature.stationsearch.StationField.Target ->
                    state.copy(
                        targetStation = result.station.crsCode,
                        targetStationName = result.station.name,
                        targetStationError = null,
                    )
                me.cniekirk.trainy.feature.stationsearch.StationField.Filter ->
                    state.copy(
                        filterStation = result.station.crsCode,
                        filterStationName = result.station.name,
                    )
            }
        }
    }

    fun onDateChanged(value: String) = intent {
        reduce {
            state.copy(
                date = value,
                dateTimeError = null,
            )
        }
    }

    fun onTimeChanged(value: String) = intent {
        reduce {
            state.copy(
                time = value,
                dateTimeError = null,
            )
        }
    }

    fun onSearchClick() = intent {
        when (val result = buildDepartureBoardSearch(state)) {
            is BuildDepartureBoardSearchResult.Error -> {
                reduce {
                    state.copy(
                        targetStationError = result.targetStationError,
                        dateTimeError = result.dateTimeError,
                    )
                }
            }

            is BuildDepartureBoardSearchResult.Success -> {
                postSideEffect(
                    SearchSideEffect.NavigateToDepartureBoard(DepartureBoardRoute(result.search))
                )
            }
        }
    }
}
