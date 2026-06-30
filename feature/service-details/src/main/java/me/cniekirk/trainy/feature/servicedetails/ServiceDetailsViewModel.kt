package me.cniekirk.trainy.feature.servicedetails

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import me.cniekirk.trainy.core.data.TrainRepository
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class ServiceDetailsViewModel(private val repository: TrainRepository) :
    ViewModel(), ContainerHost<ServiceDetailsUiState, Nothing> {
    override val container: Container<ServiceDetailsUiState, Nothing> =
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
}
