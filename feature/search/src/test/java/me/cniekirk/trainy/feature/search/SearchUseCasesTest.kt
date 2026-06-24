package me.cniekirk.trainy.feature.search

import io.mockk.every
import io.mockk.mockk
import java.util.TimeZone
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import me.cniekirk.trainy.feature.search.time.CurrentTimeProvider
import me.cniekirk.trainy.feature.search.time.SearchDateTimeFormatter
import me.cniekirk.trainy.feature.search.usecase.BuildServiceListSearchResult
import me.cniekirk.trainy.feature.search.usecase.BuildServiceListSearchUseCase
import me.cniekirk.trainy.feature.search.usecase.CreateDefaultSearchDateTimeUseCase
import me.cniekirk.trainy.feature.servicelist.ServiceListMode
import org.junit.Test

class SearchUseCasesTest {
    private val formatter = SearchDateTimeFormatter(timeZone = TimeZone.getTimeZone("UTC"))

    @Test
    fun createDefaultSearchDateTime_usesCurrentTime() {
        val currentTimeProvider =
            mockk<CurrentTimeProvider> {
                every { currentTimeMillis() } returns 1_780_313_400_000L
            }
        val useCase =
            CreateDefaultSearchDateTimeUseCase(
                formatter = formatter,
                currentTimeProvider = currentTimeProvider,
            )

        assertEquals(SearchDateTime(date = "2026-06-01", time = "11:30"), useCase())
    }

    @Test
    fun buildServiceListSearch_trimsStationsAndBuildsRequest() {
        val result =
            BuildServiceListSearchUseCase(formatter)(
                SearchUiState(
                    mode = ServiceListMode.Arriving,
                    targetStation = "  London Bridge  ",
                    filterStation = "  Brighton ",
                    date = "2026-06-01",
                    time = "11:30",
                )
            )

        val search = (result as BuildServiceListSearchResult.Success).search
        assertEquals(ServiceListMode.Arriving, search.mode)
        assertEquals("London Bridge", search.targetStation)
        assertEquals("Brighton", search.filterStation)
        assertEquals(1_780_313_400_000L, search.dateTimeMillis)
    }

    @Test
    fun buildServiceListSearch_allowsBlankFilterStation() {
        val result =
            BuildServiceListSearchUseCase(formatter)(
                SearchUiState(
                    targetStation = "London Bridge",
                    filterStation = " ",
                    date = "2026-06-01",
                    time = "11:30",
                )
            )

        val search = (result as BuildServiceListSearchResult.Success).search
        assertNull(search.filterStation)
    }

    @Test
    fun buildServiceListSearch_rejectsBlankTargetStation() {
        val result =
            BuildServiceListSearchUseCase(formatter)(
                SearchUiState(
                    targetStation = " ",
                    date = "2026-06-01",
                    time = "10:50",
                )
            )

        assertEquals(
            SearchValidationError.BlankStation,
            (result as BuildServiceListSearchResult.Error).targetStationError,
        )
    }

    @Test
    fun buildServiceListSearch_rejectsInvalidDateTime() {
        val result =
            BuildServiceListSearchUseCase(formatter)(
                SearchUiState(
                    targetStation = "London Bridge",
                    date = "2026-99-01",
                    time = "10:50",
                )
            )

        assertEquals(
            SearchValidationError.InvalidDateTime,
            (result as BuildServiceListSearchResult.Error).dateTimeError,
        )
    }
}
