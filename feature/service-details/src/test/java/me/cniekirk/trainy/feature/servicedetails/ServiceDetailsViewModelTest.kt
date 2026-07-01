package me.cniekirk.trainy.feature.servicedetails

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import me.cniekirk.trainy.core.data.TrainRepository
import me.cniekirk.trainy.core.data.TrainServiceDetails
import me.cniekirk.trainy.core.data.TrainServiceStop
import org.junit.Rule
import org.junit.Test

class ServiceDetailsViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<TrainRepository>()

    @Test
    fun loadSuccess_exposesContent() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getServiceDetails(SERVICE_ID) } returns details()
            val viewModel = ServiceDetailsViewModel(repository)

            viewModel.load(SERVICE_ID).join()

            assertEquals(details(), viewModel.container.stateFlow.value.details)
            assertFalse(viewModel.container.stateFlow.value.isLoading)
            assertFalse(viewModel.container.stateFlow.value.hasError)
        }

    @Test
    fun loadFailure_exposesError() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getServiceDetails(SERVICE_ID) } throws
                IllegalStateException("network")
            val viewModel = ServiceDetailsViewModel(repository)

            viewModel.load(SERVICE_ID).join()

            assertTrue(viewModel.container.stateFlow.value.hasError)
            assertFalse(viewModel.container.stateFlow.value.isLoading)
        }

    @Test
    fun retry_forcesAnotherLoad() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getServiceDetails(SERVICE_ID) } returns details()
            val viewModel = ServiceDetailsViewModel(repository)

            viewModel.load(SERVICE_ID).join()
            viewModel.retry(SERVICE_ID).join()

            coVerify(exactly = 2) { repository.getServiceDetails(SERVICE_ID) }
        }

    @Test
    fun duplicateLoad_isIgnored() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getServiceDetails(SERVICE_ID) } returns details()
            val viewModel = ServiceDetailsViewModel(repository)

            viewModel.load(SERVICE_ID).join()
            viewModel.load(SERVICE_ID).join()

            coVerify(exactly = 1) { repository.getServiceDetails(SERVICE_ID) }
        }

    @Test
    fun serviceIdChange_loadsNewService() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getServiceDetails(any()) } returns details()
            val viewModel = ServiceDetailsViewModel(repository)

            viewModel.load(SERVICE_ID).join()
            viewModel.load("gb-nr:L80061:2026-06-19").join()

            coVerify(exactly = 1) { repository.getServiceDetails(SERVICE_ID) }
            coVerify(exactly = 1) { repository.getServiceDetails("gb-nr:L80061:2026-06-19") }
        }

    private fun details() =
        TrainServiceDetails(
            origin = "London Waterloo",
            destination = "Exeter St Davids",
            operatorName = "South Western Railway",
            time = "09:20",
            stops =
                listOf(
                    TrainServiceStop("London Waterloo", "09:20", "8"),
                    TrainServiceStop("Exeter St Davids", "12:15", null),
                ),
        )
}

private const val SERVICE_ID = "gb-nr:L79342:2026-06-19"
