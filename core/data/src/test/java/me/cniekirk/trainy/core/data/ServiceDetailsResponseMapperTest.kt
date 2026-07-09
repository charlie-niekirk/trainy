package me.cniekirk.trainy.core.data

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.serialization.json.jsonObject
import me.cniekirk.trainy.core.network.NetworkSerialization
import me.cniekirk.trainy.core.network.generated.model.CacheStatus
import me.cniekirk.trainy.core.network.generated.model.ResponseMeta
import me.cniekirk.trainy.core.network.generated.model.ServiceResponse
import org.junit.Test

class ServiceDetailsResponseMapperTest {
    @Test
    fun mapsRouteOperatorTimeAndOrderedPublicStops() {
        val details = serviceResponse(SERVICE_JSON).toTrainServiceDetails()

        assertEquals("London Waterloo", details?.origin)
        assertEquals("Exeter St Davids", details?.destination)
        assertEquals("South Western Railway", details?.operatorName)
        assertEquals("09:20", details?.time)
        assertEquals(
            listOf(
                TrainServiceStop("London Waterloo", "09:20", "8", "WAT"),
                TrainServiceStop("Salisbury", "10:42", "4", "SAL"),
                TrainServiceStop("Exeter St Davids", "12:15", null, "EXD"),
            ),
            details?.stops,
        )
    }

    @Test
    fun arrivalIsUsedWhenDepartureIsNotAdvertised() {
        val details = serviceResponse(SERVICE_JSON).toTrainServiceDetails()

        assertEquals("12:15", details?.stops?.last()?.time)
    }

    @Test
    fun platformPrefersActualThenForecastThenPlanned() {
        val details = serviceResponse(SERVICE_JSON).toTrainServiceDetails()

        assertEquals("8", details?.stops?.get(0)?.platform)
        assertEquals("4", details?.stops?.get(1)?.platform)
        assertNull(details?.stops?.get(2)?.platform)
    }

    @Test
    fun mapsCrsCodesFromLocationShortCodes() {
        val details = serviceResponse(SERVICE_JSON).toTrainServiceDetails()

        assertEquals("WAT", details?.stops?.get(0)?.crsCode)
        assertEquals("SAL", details?.stops?.get(1)?.crsCode)
        assertEquals("EXD", details?.stops?.get(2)?.crsCode)
    }

    @Test
    fun routeFallsBackToFirstAndLastMappedStops() {
        val response = serviceResponse(SERVICE_JSON.replace(ROUTE_JSON, ""))

        val details = response.toTrainServiceDetails()

        assertEquals("London Waterloo", details?.origin)
        assertEquals("Exeter St Davids", details?.destination)
    }

    @Test
    fun malformedResponseWithoutOperatorIsRejected() {
        val response = serviceResponse(SERVICE_JSON.replace(OPERATOR_JSON, ""))

        assertNull(response.toTrainServiceDetails())
    }

    @Test
    fun malformedResponseWithoutUsableStopsIsRejected() {
        val response = serviceResponse(SERVICE_JSON.replace("\"CALL\"", "\"PASS\""))

        assertNull(response.toTrainServiceDetails())
    }

    private fun serviceResponse(json: String): ServiceResponse =
        ServiceResponse(
            data = NetworkSerialization.json.parseToJsonElement(json).jsonObject,
            meta = ResponseMeta(CacheStatus.MISS),
        )
}

private const val OPERATOR_JSON = """"operator": { "name": "South Western Railway" }"""
private const val ROUTE_JSON =
    """
    "origin": [{ "location": { "description": "London Waterloo" } }],
    "destination": [{ "location": { "description": "Exeter St Davids" } }],
    """
private const val SERVICE_JSON =
    """
    {
      "service": {
        "scheduleMetadata": {
          $OPERATOR_JSON
        },
        $ROUTE_JSON
        "locations": [
          {
            "temporalData": {
              "displayAs": "CALL",
              "departure": { "scheduleAdvertised": "2026-06-19T09:20:00Z" }
            },
            "location": { "description": "London Waterloo", "shortCodes": ["WAT"] },
            "locationMetadata": {
              "platform": { "planned": "6", "forecast": "7", "actual": "8" }
            }
          },
          {
            "temporalData": {
              "displayAs": "PASS",
              "pass": { "scheduleAdvertised": "2026-06-19T10:00:00Z" }
            },
            "location": { "description": "Andover", "shortCodes": ["ADV"] }
          },
          {
            "temporalData": {
              "displayAs": "CALL",
              "arrival": { "scheduleAdvertised": "2026-06-19T10:40:00Z" },
              "departure": { "scheduleAdvertised": "2026-06-19T10:42:00Z" }
            },
            "location": { "description": "Salisbury", "shortCodes": ["SAL"] },
            "locationMetadata": {
              "platform": { "planned": "3", "forecast": "4" }
            }
          },
          {
            "temporalData": {
              "displayAs": "CALL",
              "arrival": { "scheduleAdvertised": "2026-06-19T12:15:00Z" }
            },
            "location": { "description": "Exeter St Davids", "shortCodes": ["EXD"] }
          }
        ]
      }
    }
    """
