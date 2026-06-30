package me.cniekirk.trainy.core.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import me.cniekirk.trainy.core.database.TrackedServiceDao
import me.cniekirk.trainy.core.database.TrackedServiceEntity
import me.cniekirk.trainy.core.network.NetworkSerialization
import me.cniekirk.trainy.core.network.generated.model.BoardResponse
import me.cniekirk.trainy.core.network.generated.model.CacheStatus
import me.cniekirk.trainy.core.network.generated.model.ClientTokenResponse
import me.cniekirk.trainy.core.network.generated.model.ResponseMeta
import me.cniekirk.trainy.core.network.generated.model.ServiceResponse
import me.cniekirk.trainy.core.network.model.RttProxyBearerToken
import me.cniekirk.trainy.core.network.source.ClientTokensNetworkDataSource
import me.cniekirk.trainy.core.network.source.JourneyDataNetworkDataSource
import org.junit.Test

class DefaultTrainRepositoryTest {
    private val tokens = mockk<ClientTokensNetworkDataSource>()
    private val network = mockk<JourneyDataNetworkDataSource>()
    private val trackedServiceDao = FakeTrackedServiceDao()
    private val clock = FakeTrackedServiceClock()

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

    @Test
    fun getServiceDetails_usesClientTokenAndProxyUniqueIdentityEndpoint() = runTest {
        coEvery { tokens.createClientToken() } returns tokenResponse()
        coEvery { network.getServiceByUniqueIdentity(any(), any()) } returns serviceResponse()

        val details = repository().getServiceDetails("gb-nr:L79342:2026-06-19")

        assertEquals("London Waterloo", details.origin)
        assertEquals("Exeter St Davids", details.destination)
        assertEquals("South Western Railway", details.operatorName)
        assertEquals("09:20", details.time)
        coVerify(exactly = 1) { tokens.createClientToken() }
        coVerify(exactly = 1) {
            network.getServiceByUniqueIdentity(
                RttProxyBearerToken("token"),
                "L79342:2026-06-19",
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun getServiceDetails_rejectsMalformedResponse() = runTest {
        coEvery { tokens.createClientToken() } returns tokenResponse()
        coEvery { network.getServiceByUniqueIdentity(any(), any()) } returns
            ServiceResponse(
                data = NetworkSerialization.json.parseToJsonElement("{}").jsonObject,
                meta = ResponseMeta(CacheStatus.MISS),
            )

        repository().getServiceDetails("gb-nr:L79342:2026-06-19")
    }

    @Test
    fun trackService_persistsSnapshotWithTimestamp() = runTest {
        clock.now = 1_800_000_000_000L
        val service =
            TrainService(
                id = "gb-nr:L79342:2026-06-19",
                time = "09:20",
                destination = "Exeter St Davids",
                platform = "8",
                isPlatformConfirmed = false,
                operatorName = "South Western Railway",
            )

        repository().trackService(service)

        assertEquals(
            listOf(
                TrackedTrainService(
                    serviceId = "gb-nr:L79342:2026-06-19",
                    time = "09:20",
                    destination = "Exeter St Davids",
                    platform = "8",
                    isPlatformConfirmed = false,
                    operatorName = "South Western Railway",
                    trackedAtEpochMillis = 1_800_000_000_000L,
                )
            ),
            repository().observeTrackedServices().first(),
        )
    }

    @Test
    fun observeTrackedServiceIds_returnsUniqueTrackedIds() = runTest {
        repository().trackService(service(id = "first"))
        repository().trackService(service(id = "second"))

        assertEquals(setOf("first", "second"), repository().observeTrackedServiceIds().first())
    }

    @Test
    fun observeTrackedServices_returnsMostRecentlyTrackedFirst() = runTest {
        clock.now = 10L
        repository().trackService(service(id = "older", destination = "Salisbury"))
        clock.now = 20L
        repository().trackService(service(id = "newer", destination = "Exeter St Davids"))

        assertEquals(
            listOf("newer", "older"),
            repository().observeTrackedServices().first().map(TrackedTrainService::serviceId),
        )
    }

    @Test
    fun trackService_replacesExistingTrackedService() = runTest {
        clock.now = 10L
        repository().trackService(service(id = "same", destination = "Salisbury"))
        clock.now = 20L
        repository().trackService(service(id = "same", destination = "Exeter St Davids"))

        val trackedServices = repository().observeTrackedServices().first()
        assertEquals(1, trackedServices.size)
        assertEquals("Exeter St Davids", trackedServices.single().destination)
        assertEquals(20L, trackedServices.single().trackedAtEpochMillis)
    }

    @Test
    fun untrackService_removesTrackedService() = runTest {
        repository().trackService(service(id = "tracked"))

        repository().untrackService("tracked")

        assertTrue(repository().observeTrackedServices().first().isEmpty())
    }

    private fun repository() = DefaultTrainRepository(tokens, network, trackedServiceDao, clock)

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

    private fun serviceResponse(): ServiceResponse =
        ServiceResponse(
            data = NetworkSerialization.json.parseToJsonElement(SERVICE_DETAILS_JSON).jsonObject,
            meta = ResponseMeta(CacheStatus.MISS),
        )

    private fun service(
        id: String,
        destination: String = "Exeter St Davids",
    ) =
        TrainService(
            id = id,
            time = "09:20",
            destination = destination,
            platform = "8",
            isPlatformConfirmed = false,
            operatorName = "South Western Railway",
        )
}

private class FakeTrackedServiceClock : TrackedServiceClock {
    var now: Long = 1_800_000_000_000L

    override fun nowEpochMillis(): Long = now
}

private class FakeTrackedServiceDao : TrackedServiceDao {
    private val services = MutableStateFlow<List<TrackedServiceEntity>>(emptyList())

    override fun observeAll() = services

    override fun observeIds() =
        MutableStateFlow(services.value.map(TrackedServiceEntity::serviceId))

    override suspend fun upsert(service: TrackedServiceEntity) {
        services.value =
            (services.value.filterNot { it.serviceId == service.serviceId } + service)
                .sortedByDescending(TrackedServiceEntity::trackedAtEpochMillis)
    }

    override suspend fun delete(serviceId: String) {
        services.value = services.value.filterNot { it.serviceId == serviceId }
    }
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

private const val SERVICE_DETAILS_JSON =
    """
    {
      "service": {
        "scheduleMetadata": {
          "operator": { "name": "South Western Railway" }
        },
        "origin": [{ "location": { "description": "London Waterloo" } }],
        "destination": [{ "location": { "description": "Exeter St Davids" } }],
        "locations": [
          {
            "temporalData": {
              "displayAs": "CALL",
              "departure": { "scheduleAdvertised": "2026-06-19T09:20:00Z" }
            },
            "location": { "description": "London Waterloo" }
          },
          {
            "temporalData": {
              "displayAs": "CALL",
              "arrival": { "scheduleAdvertised": "2026-06-19T12:15:00Z" }
            },
            "location": { "description": "Exeter St Davids" }
          }
        ]
      }
    }
    """
