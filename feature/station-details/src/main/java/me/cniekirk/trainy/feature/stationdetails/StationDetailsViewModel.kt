package me.cniekirk.trainy.feature.stationdetails

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import me.cniekirk.trainy.core.data.StationRepository
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class StationDetailsViewModel(private val repository: StationRepository) :
    ViewModel(), ContainerHost<StationDetailsUiState, Nothing> {
    override val container: Container<StationDetailsUiState, Nothing> =
        container(StationDetailsUiState())

    private var loadedCrsCode: String? = null

    fun load(crsCode: String, force: Boolean = false) = intent {
        if (!force && loadedCrsCode == crsCode) return@intent
        loadedCrsCode = crsCode
        reduce { state.copy(details = null, isLoading = true, hasError = false) }
        runCatching { repository.getStationDetails(crsCode) }
            .onSuccess { details ->
                reduce { state.copy(details = details, isLoading = false, hasError = false) }
            }
            .onFailure { reduce { state.copy(details = null, isLoading = false, hasError = true) } }
    }

    fun retry(crsCode: String) = load(crsCode, force = true)
}
