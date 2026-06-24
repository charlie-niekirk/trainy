package me.cniekirk.trainy.feature.servicelist

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import me.cniekirk.trainy.core.data.ServiceListQuery
import me.cniekirk.trainy.core.data.TrainRepository
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class ServiceListViewModel(private val repository: TrainRepository) :
    ViewModel(), ContainerHost<ServiceListUiState, Nothing> {
    override val container: Container<ServiceListUiState, Nothing> = container(ServiceListUiState())

    private var loadedSearch: ServiceListSearch? = null

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
}

private fun ServiceListSearch.toQuery(): ServiceListQuery =
    ServiceListQuery(
        stationCode = targetStation,
        filterStationCode = filterStation,
        isArrivals = mode == ServiceListMode.Arriving,
        dateTimeMillis = dateTimeMillis,
    )
