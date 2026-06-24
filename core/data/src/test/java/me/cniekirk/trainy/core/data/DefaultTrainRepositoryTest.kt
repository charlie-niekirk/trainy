package me.cniekirk.trainy.core.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import me.cniekirk.trainy.core.network.NetworkSerialization
import me.cniekirk.trainy.core.network.generated.model.BoardResponse
import me.cniekirk.trainy.core.network.generated.model.CacheStatus
import me.cniekirk.trainy.core.network.generated.model.ClientTokenResponse
import me.cniekirk.trainy.core.network.generated.model.ResponseMeta
import me.cniekirk.trainy.core.network.source.ClientTokensNetworkDataSource
import me.cniekirk.trainy.core.network.source.JourneyDataNetworkDataSource
import org.junit.Test

class DefaultTrainRepositoryTest {
    private val tokens = mockk<ClientTokensNetworkDataSource>()
    private val network = mockk<JourneyDataNetworkDataSource>()

    @Test
    fun getServices_mapsForecastAndConfirmedPlatforms() = runTest {
        coEvery { tokens.createClientToken() } returns tokenResponse()
        coEvery {
            network.getBoard(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns boardResponse()

        val services = repository().getServices(query(isArrivals = false))

        assertEquals(2, services.size)
        assertEquals("09:20", services[0].time)
        assertEquals("Exeter St Davids", services[0].destination)
        assertEquals("8", services[0].platform)
        assertFalse(services[0].isPlatformConfirmed)
        assertEquals("South Western Railway", services[0].operatorName)
        assertEquals("12", services[1].platform)
        assertTrue(services[1].isPlatformConfirmed)
        coVerify {
            network.getBoard(
                any(),
                code = "WAT",
                filterFrom = null,
                filterTo = "SAL",
                timeFrom = any(),
                timeTo = null,
                timeWindow = null,
                timeTolerance = null,
                stpFilter = null,
            )
        }
    }

    @Test
    fun getServices_forArrivals_usesFilterFromAndArrivalTime() = runTest {
        coEvery { tokens.createClientToken() } returns tokenResponse()
        coEvery {
            network.getBoard(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns boardResponse()

        val services = repository().getServices(query(isArrivals = true))

        assertEquals("09:18", services.first().time)
        coVerify {
            network.getBoard(
                any(),
                code = "WAT",
                filterFrom = "SAL",
                filterTo = null,
                timeFrom = any(),
                timeTo = null,
                timeWindow = null,
                timeTolerance = null,
                stpFilter = null,
            )
        }
    }

    private fun repository() = DefaultTrainRepository(tokens, network)

    private fun query(isArrivals: Boolean) =
        ServiceListQuery(
            stationCode = "WAT",
            filterStationCode = "SAL",
            isArrivals = isArrivals,
            dateTimeMillis = 1_781_856_000_000L,
        )

    private fun tokenResponse() =
        ClientTokenResponse(
            token = "token",
            tokenType = ClientTokenResponse.TokenType.BEARER,
            expiresAt = "2099-01-01T00:00:00Z",
        )

    private fun boardResponse(): BoardResponse =
        BoardResponse(
            data = NetworkSerialization.json.parseToJsonElement(BOARD_JSON).jsonObject,
            meta = ResponseMeta(CacheStatus.MISS),
        )
}

private const val BOARD_JSON =
    """
    {
      "services": [
        {
          "temporalData": {
            "departure": { "scheduleAdvertised": "2026-06-19T09:20:00" },
            "arrival": { "scheduleAdvertised": "2026-06-19T09:18:00" }
          },
          "locationMetadata": { "platform": { "planned": "8", "forecast": "8" } },
          "scheduleMetadata": {
            "uniqueIdentity": "gb-nr:L79342:2026-06-19",
            "operator": { "name": "South Western Railway" }
          },
          "destination": [{ "location": { "description": "Exeter St Davids" } }]
        },
        {
          "temporalData": {
            "departure": { "scheduleAdvertised": "2026-06-19T09:50:00" },
            "arrival": { "scheduleAdvertised": "2026-06-19T09:48:00" }
          },
          "locationMetadata": { "platform": { "planned": "6", "actual": "12" } },
          "scheduleMetadata": {
            "uniqueIdentity": "gb-nr:L80061:2026-06-19",
            "operator": { "name": "South Western Railway" }
          },
          "destination": [{ "location": { "description": "Yeovil Junction" } }]
        }
      ]
    }
    """
