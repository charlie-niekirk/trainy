package me.cniekirk.trainy.feature.servicelist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.cniekirk.trainy.core.data.TrainRepository
import me.cniekirk.trainy.core.data.TrainService
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ServiceListViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<TrainRepository>()
    private val trackedServiceIds = MutableStateFlow<Set<String>>(emptySet())
    private val search =
        ServiceListSearch(
            mode = ServiceListMode.Departing,
            targetStation = "WAT",
            targetStationName = "London Waterloo",
            filterStation = "SAL",
            filterStationName = "Salisbury",
            dateTimeMillis = 1_781_856_000_000L,
        )

    @Test
    fun load_fetchesServicesFromNavigationSearch() =
        runTest(mainDispatcherRule.testDispatcher) {
            val service = TrainService("id", "09:20", "Exeter St Davids", "8", false, "SWR")
            coEvery { repository.getServices(any()) } returns listOf(service)
            val viewModel = viewModel()

            viewModel.load(search).join()

            assertEquals(listOf(service), viewModel.container.stateFlow.value.services)
            assertFalse(viewModel.container.stateFlow.value.isLoading)
            assertFalse(viewModel.container.stateFlow.value.hasError)
            coVerify(exactly = 1) {
                repository.getServices(
                    match {
                        it.stationCode == "WAT" &&
                            it.filterStationCode == "SAL" &&
                            !it.isArrivals &&
                            it.dateTimeMillis == search.dateTimeMillis
                    }
                )
            }
        }

    @Test
    fun loadFailure_showsRetryState() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getServices(any()) } throws IllegalStateException("network")
            val viewModel = viewModel()

            viewModel.load(search).join()

            assertTrue(viewModel.container.stateFlow.value.hasError)
            assertFalse(viewModel.container.stateFlow.value.isLoading)
        }

    @Test
    fun retry_requestsSameSearchAgain() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getServices(any()) } returns emptyList()
            val viewModel = viewModel()

            viewModel.load(search).join()
            viewModel.retry(search).join()

            coVerify(exactly = 2) { repository.getServices(any()) }
        }

    @Test
    fun trackedServiceIds_areObserved() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()

            trackedServiceIds.value = setOf("tracked")
            advanceUntilIdle()

            assertEquals(setOf("tracked"), viewModel.container.stateFlow.value.trackedServiceIds)
        }

    @Test
    fun trackingClick_tracksUntrackedService() =
        runTest(mainDispatcherRule.testDispatcher) {
            val service = TrainService("id", "09:20", "Exeter St Davids", "8", false, "SWR")
            coEvery { repository.trackService(service) } returns Unit
            val viewModel = viewModel()

            viewModel.onTrackingClick(service).join()

            coVerify { repository.trackService(service) }
            coVerify(exactly = 0) { repository.untrackService(any()) }
        }

    @Test
    fun trackingClick_untracksTrackedService() =
        runTest(mainDispatcherRule.testDispatcher) {
            val service = TrainService("id", "09:20", "Exeter St Davids", "8", false, "SWR")
            trackedServiceIds.value = setOf("id")
            coEvery { repository.untrackService("id") } returns Unit
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.onTrackingClick(service).join()

            coVerify { repository.untrackService("id") }
            coVerify(exactly = 0) { repository.trackService(any()) }
        }

    private fun viewModel(): ServiceListViewModel {
        every { repository.observeTrackedServiceIds() } returns trackedServiceIds
        return ServiceListViewModel(repository)
    }
}
