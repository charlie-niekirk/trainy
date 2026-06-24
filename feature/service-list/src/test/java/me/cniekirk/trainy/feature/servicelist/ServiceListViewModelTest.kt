package me.cniekirk.trainy.feature.servicelist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import me.cniekirk.trainy.core.data.TrainRepository
import me.cniekirk.trainy.core.data.TrainService
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ServiceListViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<TrainRepository>()
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
            val viewModel = ServiceListViewModel(repository)

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
            val viewModel = ServiceListViewModel(repository)

            viewModel.load(search).join()

            assertTrue(viewModel.container.stateFlow.value.hasError)
            assertFalse(viewModel.container.stateFlow.value.isLoading)
        }

    @Test
    fun retry_requestsSameSearchAgain() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getServices(any()) } returns emptyList()
            val viewModel = ServiceListViewModel(repository)

            viewModel.load(search).join()
            viewModel.retry(search).join()

            coVerify(exactly = 2) { repository.getServices(any()) }
        }
}
