package me.cniekirk.trainy.feature.servicedetails

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import me.cniekirk.trainy.core.data.TrainRepository
import me.cniekirk.trainy.core.data.TrainServiceStop
import me.cniekirk.trainy.feature.stationdetails.StationDetailsRoute
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class ServiceDetailsViewModel(private val repository: TrainRepository) :
    ViewModel(), ContainerHost<ServiceDetailsUiState, ServiceDetailsSideEffect> {
    override val container: Container<ServiceDetailsUiState, ServiceDetailsSideEffect> =
        container(ServiceDetailsUiState())

    private var loadedServiceId: String? = null

    fun load(serviceId: String, force: Boolean = false) = intent {
        if (!force && loadedServiceId == serviceId) return@intent
        loadedServiceId = serviceId
        reduce { state.copy(details = null, isLoading = true, hasError = false) }
        runCatching { repository.getServiceDetails(serviceId) }
            .onSuccess { details ->
                reduce { state.copy(details = details, isLoading = false, hasError = false) }
            }
            .onFailure { reduce { state.copy(details = null, isLoading = false, hasError = true) } }
    }

    fun retry(serviceId: String) = load(serviceId, force = true)

    fun onStopSelected(stop: TrainServiceStop) = intent {
        val crs = stop.crsCode ?: return@intent
        postSideEffect(
            ServiceDetailsSideEffect.NavigateToStation(
                StationDetailsRoute(crsCode = crs, name = stop.name)
            )
        )
    }
}
