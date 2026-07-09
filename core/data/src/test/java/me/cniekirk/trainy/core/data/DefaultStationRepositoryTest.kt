package me.cniekirk.trainy.core.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.cniekirk.trainy.core.database.CachedStationDao
import me.cniekirk.trainy.core.database.CachedStationEntity
import me.cniekirk.trainy.core.network.generated.model.CacheStatus
import me.cniekirk.trainy.core.network.generated.model.ClientTokenResponse
import me.cniekirk.trainy.core.network.generated.model.CollectionMeta
import me.cniekirk.trainy.core.network.generated.model.NationalRailStation
import me.cniekirk.trainy.core.network.generated.model.ResponseMeta
import me.cniekirk.trainy.core.network.generated.model.StationResponse
import me.cniekirk.trainy.core.network.generated.model.StationSummary
import me.cniekirk.trainy.core.network.generated.model.StationsResponse
import me.cniekirk.trainy.core.network.source.ClientTokensNetworkDataSource
import me.cniekirk.trainy.core.network.source.JourneyDataNetworkDataSource
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultStationRepositoryTest {
    private val dao = mockk<CachedStationDao>()
    private val tokens = mockk<ClientTokensNetworkDataSource>()
    private val network = mockk<JourneyDataNetworkDataSource>()
    private val clock = mockk<StationCacheClock>()
    private val now = 1_800_000_000_000L

    @Test
    fun freshCache_isReturnedWithoutNetworkRequest() = runTest {
        every { clock.nowEpochMillis() } returns now
        coEvery { dao.latestFetchEpochMillis() } returns now - 6L * 24 * 60 * 60 * 1_000
        coEvery { dao.getAll() } returns listOf(cachedStation("KGX", "London Kings Cross"))

        val result = repository().getStations()

        assertEquals(listOf(Station("London Kings Cross", "KGX")), result)
        coVerify(exactly = 0) { tokens.createClientToken() }
    }

    @Test
    fun staleCache_isReplacedFromStationsApi() = runTest {
        every { clock.nowEpochMillis() } returns now
        coEvery { dao.latestFetchEpochMillis() } returns now - 8L * 24 * 60 * 60 * 1_000
        coEvery { tokens.createClientToken() } returns
            ClientTokenResponse("token", ClientTokenResponse.TokenType.BEARER, "later")
        coEvery { network.getStations(any()) } returns
            StationsResponse(
                data = listOf(StationSummary("EUS", "London Euston")),
                meta = CollectionMeta(CacheStatus.MISS, 1),
            )
        coEvery { dao.replaceAll(any()) } returns Unit
        coEvery { dao.getAll() } returns listOf(cachedStation("EUS", "London Euston"))

        val result = repository().getStations()

        assertEquals(listOf(Station("London Euston", "EUS")), result)
        coVerify { dao.replaceAll(listOf(cachedStation("EUS", "London Euston"))) }
    }

    @Test
    fun getStationDetails_mapsNetworkResponse() = runTest {
        coEvery { tokens.createClientToken() } returns
            ClientTokenResponse("token", ClientTokenResponse.TokenType.BEARER, "later")
        coEvery { network.getStation(any(), "WAT") } returns
            StationResponse(
                data = NationalRailStation(name = "London Waterloo", crsCode = "WAT"),
                meta = ResponseMeta(CacheStatus.MISS),
            )

        val result = repository().getStationDetails("WAT")

        assertEquals("London Waterloo", result.name)
        assertEquals("WAT", result.crsCode)
        coVerify { network.getStation(any(), "WAT") }
    }

    private fun repository() = DefaultStationRepository(dao, tokens, network, clock)

    private fun cachedStation(code: String, name: String) = CachedStationEntity(code, name, now)
}
