package me.cniekirk.trainy.feature.servicelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.launch
import me.cniekirk.trainy.core.data.ServiceListQuery
import me.cniekirk.trainy.core.data.TrainRepository
import me.cniekirk.trainy.core.data.TrainService
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class ServiceListViewModel(private val repository: TrainRepository) :
    ViewModel(), ContainerHost<ServiceListUiState, ServiceListSideEffect> {
    override val container: Container<ServiceListUiState, ServiceListSideEffect> =
        container(ServiceListUiState())

    private var loadedSearch: ServiceListSearch? = null

    init {
        viewModelScope.launch {
            repository.observeTrackedServiceIds().collect { trackedServiceIds ->
                intent { reduce { state.copy(trackedServiceIds = trackedServiceIds) } }
            }
        }
    }

    fun load(search: ServiceListSearch, force: Boolean = false) = intent {
        if (!force && loadedSearch == search) return@intent
        loadedSearch = search
        reduce { state.copy(isLoading = true, hasError = false) }
        runCatching { repository.getServices(search.toQuery()) }
            .onSuccess { services ->
                reduce {
                    state.copy(
                        services = services,
                        isLoading = false,
                        hasError = false,
                    )
                }
            }
            .onFailure {
                reduce { state.copy(isLoading = false, hasError = true) }
            }
    }

    fun retry(search: ServiceListSearch) = load(search, force = true)

    fun onTrackingClick(service: TrainService) = intent {
        runCatching {
                if (service.id in state.trackedServiceIds) {
                    repository.untrackService(service.id)
                } else {
                    repository.trackService(service)
                }
            }
            .onFailure { postSideEffect(ServiceListSideEffect.ShowTrackingError) }
    }
}

private fun ServiceListSearch.toQuery(): ServiceListQuery =
    ServiceListQuery(
        stationCode = targetStation,
        filterStationCode = filterStation,
        isArrivals = mode == ServiceListMode.Arriving,
        dateTimeMillis = dateTimeMillis,
    )
