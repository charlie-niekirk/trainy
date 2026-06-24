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
import me.cniekirk.trainy.feature.search.usecase.BuildServiceListSearchResult
import me.cniekirk.trainy.feature.search.usecase.BuildServiceListSearchUseCase
import me.cniekirk.trainy.feature.search.usecase.CreateDefaultSearchDateTimeUseCase
import me.cniekirk.trainy.feature.servicelist.ServiceListMode
import me.cniekirk.trainy.feature.servicelist.ServiceListRoute
import me.cniekirk.trainy.feature.servicelist.ServiceListSearch
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
                    mode = ServiceListMode.Departing,
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

            viewModel.onModeSelected(ServiceListMode.Arriving).join()

            assertEquals(ServiceListMode.Arriving, viewModel.container.stateFlow.value.mode)
        }

    @Test
    fun searchClick_withValidState_postsServiceListRoute() =
        runTest(mainDispatcherRule.testDispatcher) {
            val search =
                ServiceListSearch(
                    mode = ServiceListMode.Arriving,
                    targetStation = "London Bridge",
                    filterStation = "Brighton",
                    dateTimeMillis = 1_780_313_400_000L,
                )
            val buildServiceListSearchUseCase = mockk<BuildServiceListSearchUseCase>()
            every { buildServiceListSearchUseCase(any<SearchUiState>()) } returns
                BuildServiceListSearchResult.Success(search)
            val viewModel =
                createViewModel(buildServiceListSearchUseCase = buildServiceListSearchUseCase)
            val sideEffect = async { viewModel.container.sideEffectFlow.first() }

            viewModel.onModeSelected(ServiceListMode.Arriving).join()
            viewModel.onTargetStationChanged(" London Bridge ").join()
            viewModel.onFilterStationChanged(" Brighton ").join()
            viewModel.onSearchClick().join()

            assertEquals(
                SearchSideEffect.NavigateToServiceList(ServiceListRoute(search)),
                sideEffect.await(),
            )
            verify { buildServiceListSearchUseCase(any<SearchUiState>()) }
        }

    @Test
    fun searchClick_withBlankTarget_updatesValidationError() =
        runTest(mainDispatcherRule.testDispatcher) {
            val buildServiceListSearchUseCase = mockk<BuildServiceListSearchUseCase>()
            every { buildServiceListSearchUseCase(any<SearchUiState>()) } returns
                BuildServiceListSearchResult.Error(
                    targetStationError = SearchValidationError.BlankStation
                )
            val viewModel =
                createViewModel(buildServiceListSearchUseCase = buildServiceListSearchUseCase)

            viewModel.onSearchClick().join()

            assertEquals(
                SearchValidationError.BlankStation,
                viewModel.container.stateFlow.value.targetStationError,
            )
            verify { buildServiceListSearchUseCase(any<SearchUiState>()) }
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
        createDefaultSearchDateTimeUseCase: CreateDefaultSearchDateTimeUseCase =
            mockCreateDefaultSearchDateTime(),
        buildServiceListSearchUseCase: BuildServiceListSearchUseCase = mockBuildServiceListSearch(),
    ): SearchViewModel =
        SearchViewModel(
            createDefaultSearchDateTimeUseCase = createDefaultSearchDateTimeUseCase,
            buildServiceListSearchUseCase = buildServiceListSearchUseCase,
        )

    private fun mockCreateDefaultSearchDateTime(): CreateDefaultSearchDateTimeUseCase {
        val createDefaultSearchDateTimeUseCase = mockk<CreateDefaultSearchDateTimeUseCase>()
        every { createDefaultSearchDateTimeUseCase() } returns defaultDateTime
        return createDefaultSearchDateTimeUseCase
    }

    private fun mockBuildServiceListSearch(): BuildServiceListSearchUseCase {
        val buildServiceListSearchUseCase = mockk<BuildServiceListSearchUseCase>()
        every { buildServiceListSearchUseCase(any<SearchUiState>()) } returns
            BuildServiceListSearchResult.Success(
                ServiceListSearch(
                    mode = ServiceListMode.Departing,
                    targetStation = "London Bridge",
                    filterStation = null,
                    dateTimeMillis = 1_780_313_400_000L,
                )
            )
        return buildServiceListSearchUseCase
    }
}
