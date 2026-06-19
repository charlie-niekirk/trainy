package me.cniekirk.trainy.feature.search

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.cniekirk.trainy.core.data.Station
import me.cniekirk.trainy.feature.stationsearch.StationField
import me.cniekirk.trainy.feature.stationsearch.StationSelectionResult
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val defaultDateTime = SearchDateTime(date = "2026-06-01", time = "11:30")

    @Test
    fun initialState_defaultsDateTimeToNowAndDepartingMode() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            assertEquals(
                SearchUiState(
                    mode = SearchMode.Departing,
                    date = "2026-06-01",
                    time = "11:30",
                ),
                viewModel.container.stateFlow.value,
            )
        }

    @Test
    fun selectingArriving_updatesStationLabelsMode() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onModeSelected(SearchMode.Arriving).join()

            assertEquals(SearchMode.Arriving, viewModel.container.stateFlow.value.mode)
        }

    @Test
    fun searchClick_withValidState_postsDepartureBoardRoute() =
        runTest(mainDispatcherRule.testDispatcher) {
            val search =
                DepartureBoardSearch(
                    mode = SearchMode.Arriving,
                    targetStation = "London Bridge",
                    filterStation = "Brighton",
                    dateTimeMillis = 1_780_313_400_000L,
                )
            val buildDepartureBoardSearch = mockk<BuildDepartureBoardSearch>()
            every { buildDepartureBoardSearch(any<SearchUiState>()) } returns
                BuildDepartureBoardSearchResult.Success(search)
            val viewModel = createViewModel(buildDepartureBoardSearch = buildDepartureBoardSearch)
            val sideEffect = async { viewModel.container.sideEffectFlow.first() }

            viewModel.onModeSelected(SearchMode.Arriving).join()
            viewModel.onTargetStationChanged(" London Bridge ").join()
            viewModel.onFilterStationChanged(" Brighton ").join()
            viewModel.onSearchClick().join()

            assertEquals(
                SearchSideEffect.NavigateToDepartureBoard(DepartureBoardRoute(search)),
                sideEffect.await(),
            )
            verify { buildDepartureBoardSearch(any<SearchUiState>()) }
        }

    @Test
    fun searchClick_withBlankTarget_updatesValidationError() =
        runTest(mainDispatcherRule.testDispatcher) {
            val buildDepartureBoardSearch = mockk<BuildDepartureBoardSearch>()
            every { buildDepartureBoardSearch(any<SearchUiState>()) } returns
                BuildDepartureBoardSearchResult.Error(
                    targetStationError = SearchValidationError.BlankStation
                )
            val viewModel = createViewModel(buildDepartureBoardSearch = buildDepartureBoardSearch)

            viewModel.onSearchClick().join()

            assertEquals(
                SearchValidationError.BlankStation,
                viewModel.container.stateFlow.value.targetStationError,
            )
            verify { buildDepartureBoardSearch(any<SearchUiState>()) }
        }

    @Test
    fun stationResult_updatesSelectedFieldWithCrsAndName() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel
                .onStationSelected(
                    StationSelectionResult(
                        StationField.Target,
                        Station("London Kings Cross", "KGX"),
                    )
                )
                .join()

            assertEquals("KGX", viewModel.container.stateFlow.value.targetStation)
            assertEquals(
                "London Kings Cross",
                viewModel.container.stateFlow.value.targetStationName,
            )
        }

    private fun createViewModel(
        createDefaultSearchDateTime: CreateDefaultSearchDateTime =
            mockCreateDefaultSearchDateTime(),
        buildDepartureBoardSearch: BuildDepartureBoardSearch = mockBuildDepartureBoardSearch(),
    ): SearchViewModel =
        SearchViewModel(
            createDefaultSearchDateTime = createDefaultSearchDateTime,
            buildDepartureBoardSearch = buildDepartureBoardSearch,
        )

    private fun mockCreateDefaultSearchDateTime(): CreateDefaultSearchDateTime {
        val createDefaultSearchDateTime = mockk<CreateDefaultSearchDateTime>()
        every { createDefaultSearchDateTime() } returns defaultDateTime
        return createDefaultSearchDateTime
    }

    private fun mockBuildDepartureBoardSearch(): BuildDepartureBoardSearch {
        val buildDepartureBoardSearch = mockk<BuildDepartureBoardSearch>()
        every { buildDepartureBoardSearch(any<SearchUiState>()) } returns
            BuildDepartureBoardSearchResult.Success(
                DepartureBoardSearch(
                    mode = SearchMode.Departing,
                    targetStation = "London Bridge",
                    filterStation = null,
                    dateTimeMillis = 1_780_313_400_000L,
                )
            )
        return buildDepartureBoardSearch
    }
}
