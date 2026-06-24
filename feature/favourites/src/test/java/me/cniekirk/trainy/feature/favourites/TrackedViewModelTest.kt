package me.cniekirk.trainy.feature.favourites

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.cniekirk.trainy.core.data.ServiceListQuery
import me.cniekirk.trainy.core.data.TrackedTrainService
import me.cniekirk.trainy.core.data.TrainRepository
import me.cniekirk.trainy.core.data.TrainService
import me.cniekirk.trainy.feature.servicedetails.ServiceDetailsRoute
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrackedViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val trackedServices = MutableStateFlow<List<TrackedTrainService>>(emptyList())
    private val repository = FakeTrainRepository(trackedServices)

    @Test
    fun trackedServices_areObserved() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = TrackedViewModel(repository)
            val service = trackedService(serviceId = "tracked")
            backgroundScope.launch { viewModel.container.stateFlow.collect() }

            trackedServices.value = listOf(service)
            advanceUntilIdle()

            assertEquals(
                TrackedUiState(listOf(service), isLoading = false),
                viewModel.container.stateFlow.value,
            )
        }

    @Test
    fun trackedService_mapsToServiceDetailsRoute() {
        assertEquals(
            ServiceDetailsRoute("tracked"),
            trackedService("tracked").toServiceDetailsRoute(),
        )
    }

    private fun trackedService(serviceId: String) =
        TrackedTrainService(
            serviceId = serviceId,
            time = "09:20",
            destination = "Exeter St Davids",
            platform = "8",
            isPlatformConfirmed = false,
            operatorName = "South Western Railway",
            trackedAtEpochMillis = 1_800_000_000_000L,
        )
}

private class FakeTrainRepository(private val trackedServices: Flow<List<TrackedTrainService>>) :
    TrainRepository {
    override suspend fun getServices(query: ServiceListQuery): List<TrainService> = emptyList()

    override fun observeTrackedServices(): Flow<List<TrackedTrainService>> = trackedServices

    override fun observeTrackedServiceIds(): Flow<Set<String>> = MutableStateFlow(emptySet())

    override suspend fun trackService(service: TrainService) = Unit

    override suspend fun untrackService(serviceId: String) = Unit
}
