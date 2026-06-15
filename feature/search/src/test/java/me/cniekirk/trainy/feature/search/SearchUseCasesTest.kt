package me.cniekirk.trainy.feature.search

import io.mockk.every
import io.mockk.mockk
import java.util.TimeZone
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
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
            CreateDefaultSearchDateTime(
                formatter = formatter,
                currentTimeProvider = currentTimeProvider,
            )

        assertEquals(SearchDateTime(date = "2026-06-01", time = "11:30"), useCase())
    }

    @Test
    fun buildDepartureBoardSearch_trimsStationsAndBuildsRequest() {
        val result =
            BuildDepartureBoardSearch(formatter)(
                SearchUiState(
                    mode = SearchMode.Arriving,
                    targetStation = "  London Bridge  ",
                    filterStation = "  Brighton ",
                    date = "2026-06-01",
                    time = "11:30",
                )
            )

        val search = (result as BuildDepartureBoardSearchResult.Success).search
        assertEquals(SearchMode.Arriving, search.mode)
        assertEquals("London Bridge", search.targetStation)
        assertEquals("Brighton", search.filterStation)
        assertEquals(1_780_313_400_000L, search.dateTimeMillis)
    }

    @Test
    fun buildDepartureBoardSearch_allowsBlankFilterStation() {
        val result =
            BuildDepartureBoardSearch(formatter)(
                SearchUiState(
                    targetStation = "London Bridge",
                    filterStation = " ",
                    date = "2026-06-01",
                    time = "11:30",
                )
            )

        val search = (result as BuildDepartureBoardSearchResult.Success).search
        assertNull(search.filterStation)
    }

    @Test
    fun buildDepartureBoardSearch_rejectsBlankTargetStation() {
        val result =
            BuildDepartureBoardSearch(formatter)(
                SearchUiState(
                    targetStation = " ",
                    date = "2026-06-01",
                    time = "10:50",
                )
            )

        assertEquals(
            SearchValidationError.BlankStation,
            (result as BuildDepartureBoardSearchResult.Error).targetStationError,
        )
    }

    @Test
    fun buildDepartureBoardSearch_rejectsInvalidDateTime() {
        val result =
            BuildDepartureBoardSearch(formatter)(
                SearchUiState(
                    targetStation = "London Bridge",
                    date = "2026-99-01",
                    time = "10:50",
                )
            )

        assertEquals(
            SearchValidationError.InvalidDateTime,
            (result as BuildDepartureBoardSearchResult.Error).dateTimeError,
        )
    }
}
