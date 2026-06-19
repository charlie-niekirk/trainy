package me.cniekirk.trainy.feature.stationsearch

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.cniekirk.trainy.core.data.Station
import me.cniekirk.trainy.core.data.StationRepository
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StationSearchViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()
    private val stations = listOf(Station("London Kings Cross", "KGX"), Station("Brighton", "BTN"))

    @Test
    fun query_filtersByNameAndCrsCodeIgnoringCase() =
        runTest(dispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.onQueryChanged("kings")
            assertEquals(listOf(stations[0]), viewModel.container.stateFlow.value.stations)

            viewModel.onQueryChanged("btn")
            assertEquals(listOf(stations[1]), viewModel.container.stateFlow.value.stations)
        }

    @Test
    fun selectingStation_returnsFieldAndStation() =
        runTest(dispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            advanceUntilIdle()
            val effect = async { viewModel.container.sideEffectFlow.first() }

            viewModel.onStationSelected(StationField.Filter, stations[1]).join()

            assertEquals(
                StationSearchSideEffect.ReturnSelection(
                    StationSelectionResult(StationField.Filter, stations[1])
                ),
                effect.await(),
            )
        }

    private fun viewModel(): StationSearchViewModel {
        val repository = mockk<StationRepository>()
        coEvery { repository.getStations() } returns stations
        return StationSearchViewModel(repository)
    }
}
