package me.cniekirk.trainy.feature.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.launch
import me.cniekirk.trainy.core.data.TrackedTrainService
import me.cniekirk.trainy.core.data.TrainRepository
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.container

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class TrackedViewModel(private val repository: TrainRepository) :
    ViewModel(), ContainerHost<TrackedUiState, Nothing> {
    override val container: Container<TrackedUiState, Nothing> = container(TrackedUiState())

    init {
        viewModelScope.launch {
            repository.observeTrackedServices().collect { services ->
                blockingIntent {
                    reduce {
                        state.copy(
                            services = services.map(TrackedTrainService::toUiModel),
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }
}

private fun TrackedTrainService.toUiModel(): TrackedServiceUiModel =
    TrackedServiceUiModel(
        serviceId = serviceId,
        time = time,
        destination = destination,
        platform = platform,
        isPlatformConfirmed = isPlatformConfirmed,
        operatorName = operatorName,
    )
