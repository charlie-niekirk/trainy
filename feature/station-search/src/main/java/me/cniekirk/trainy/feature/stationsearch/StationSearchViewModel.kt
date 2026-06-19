package me.cniekirk.trainy.feature.stationsearch

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import me.cniekirk.trainy.core.data.Station
import me.cniekirk.trainy.core.data.StationRepository
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.container

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class StationSearchViewModel(private val repository: StationRepository) :
    ViewModel(), ContainerHost<StationSearchUiState, StationSearchSideEffect> {

    private var allStations: List<Station> = emptyList()

    override val container: Container<StationSearchUiState, StationSearchSideEffect> =
        container(StationSearchUiState()) {
            loadStations()
        }

    fun onQueryChanged(value: String) = blockingIntent {
        reduce { state.copy(query = value, stations = filterStations(value)) }
    }

    fun onStationSelected(field: StationField, station: Station) = intent {
        postSideEffect(
            StationSearchSideEffect.ReturnSelection(StationSelectionResult(field, station))
        )
    }

    fun retry() = loadStations()

    private fun loadStations() = intent {
        reduce { state.copy(isLoading = true, hasError = false) }
        runCatching { repository.getStations() }
            .onSuccess { stations ->
                allStations = stations
                reduce {
                    state.copy(
                        stations = filterStations(state.query),
                        isLoading = false,
                        hasError = false,
                    )
                }
            }
            .onFailure {
                reduce { state.copy(isLoading = false, hasError = true) }
            }
    }

    private fun filterStations(query: String): List<Station> {
        val term = query.trim()
        if (term.isEmpty()) return allStations
        return allStations.filter {
            it.name.contains(term, ignoreCase = true) ||
                it.crsCode.contains(term, ignoreCase = true)
        }
    }
}
