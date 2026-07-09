package me.cniekirk.trainy.feature.stationdetails

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import me.cniekirk.trainy.core.data.StationDetails
import me.cniekirk.trainy.core.data.StationRepository
import org.junit.Rule
import org.junit.Test

class StationDetailsViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<StationRepository>()

    @Test
    fun loadSuccess_exposesContent() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getStationDetails(CRS_CODE) } returns details()
            val viewModel = StationDetailsViewModel(repository)

            viewModel.load(CRS_CODE).join()

            assertEquals(details(), viewModel.container.stateFlow.value.details)
            assertFalse(viewModel.container.stateFlow.value.isLoading)
            assertFalse(viewModel.container.stateFlow.value.hasError)
        }

    @Test
    fun loadFailure_exposesError() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getStationDetails(CRS_CODE) } throws
                IllegalStateException("network")
            val viewModel = StationDetailsViewModel(repository)

            viewModel.load(CRS_CODE).join()

            assertTrue(viewModel.container.stateFlow.value.hasError)
            assertFalse(viewModel.container.stateFlow.value.isLoading)
        }

    @Test
    fun retry_forcesAnotherLoad() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getStationDetails(CRS_CODE) } returns details()
            val viewModel = StationDetailsViewModel(repository)

            viewModel.load(CRS_CODE).join()
            viewModel.retry(CRS_CODE).join()

            coVerify(exactly = 2) { repository.getStationDetails(CRS_CODE) }
        }

    @Test
    fun duplicateLoad_isIgnored() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getStationDetails(CRS_CODE) } returns details()
            val viewModel = StationDetailsViewModel(repository)

            viewModel.load(CRS_CODE).join()
            viewModel.load(CRS_CODE).join()

            coVerify(exactly = 1) { repository.getStationDetails(CRS_CODE) }
        }

    @Test
    fun crsCodeChange_loadsNewStation() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getStationDetails(any()) } returns details()
            val viewModel = StationDetailsViewModel(repository)

            viewModel.load(CRS_CODE).join()
            viewModel.load("PAD").join()

            coVerify(exactly = 1) { repository.getStationDetails(CRS_CODE) }
            coVerify(exactly = 1) { repository.getStationDetails("PAD") }
        }

    private fun details() =
        StationDetails(
            name = "London Waterloo",
            crsCode = CRS_CODE,
            operatorName = "South Western Railway",
            staffingLevel = "Full Time",
        )
}

private const val CRS_CODE = "WAT"
