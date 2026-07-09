package me.cniekirk.trainy.core.data

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import me.cniekirk.trainy.core.network.NetworkSerialization
import me.cniekirk.trainy.core.network.generated.model.CacheStatus
import me.cniekirk.trainy.core.network.generated.model.NationalRailAddress
import me.cniekirk.trainy.core.network.generated.model.NationalRailCarPark
import me.cniekirk.trainy.core.network.generated.model.NationalRailCarParkCharges
import me.cniekirk.trainy.core.network.generated.model.NationalRailCycleSpaces
import me.cniekirk.trainy.core.network.generated.model.NationalRailCycling
import me.cniekirk.trainy.core.network.generated.model.NationalRailDropOffPickUp
import me.cniekirk.trainy.core.network.generated.model.NationalRailDropOffPickUpPoint
import me.cniekirk.trainy.core.network.generated.model.NationalRailHelpAndSupport
import me.cniekirk.trainy.core.network.generated.model.NationalRailHelpPoints
import me.cniekirk.trainy.core.network.generated.model.NationalRailLift
import me.cniekirk.trainy.core.network.generated.model.NationalRailLifts
import me.cniekirk.trainy.core.network.generated.model.NationalRailLocation
import me.cniekirk.trainy.core.network.generated.model.NationalRailLoungeAndWaiting
import me.cniekirk.trainy.core.network.generated.model.NationalRailPlatform
import me.cniekirk.trainy.core.network.generated.model.NationalRailPlatformFacilities
import me.cniekirk.trainy.core.network.generated.model.NationalRailStation
import me.cniekirk.trainy.core.network.generated.model.NationalRailStationAccessibility
import me.cniekirk.trainy.core.network.generated.model.NationalRailStationAlerts
import me.cniekirk.trainy.core.network.generated.model.NationalRailStationCarParks
import me.cniekirk.trainy.core.network.generated.model.NationalRailStationFacilities
import me.cniekirk.trainy.core.network.generated.model.NationalRailStationFacility
import me.cniekirk.trainy.core.network.generated.model.NationalRailStationOperator
import me.cniekirk.trainy.core.network.generated.model.NationalRailStepFreeCategory
import me.cniekirk.trainy.core.network.generated.model.NationalRailTicketBuying
import me.cniekirk.trainy.core.network.generated.model.NationalRailToilets
import me.cniekirk.trainy.core.network.generated.model.NationalRailToiletsAndChanging
import me.cniekirk.trainy.core.network.generated.model.NationalRailTransportLinkItem
import me.cniekirk.trainy.core.network.generated.model.NationalRailTransportLinks
import me.cniekirk.trainy.core.network.generated.model.ResponseMeta
import me.cniekirk.trainy.core.network.generated.model.StationResponse
import org.junit.Test

class StationDetailsResponseMapperTest {
    @Test
    fun mapsCoreIdentityAddressAndLocation() {
        val details = stationResponse().toStationDetails()

        assertEquals("London Waterloo", details.name)
        assertEquals("WAT", details.crsCode)
        assertEquals("Part Time", details.staffingLevel)
        assertEquals("South Western Railway", details.operatorName)
        assertEquals(
            StationAddress(lines = listOf("Station Approach", "London"), postcode = "SE1 8SW"),
            details.address,
        )
        assertEquals(StationCoordinates(51.5031, -0.1132), details.location)
    }

    @Test
    fun mapsAlertsWithOnlyAlertText() {
        val details =
            StationResponse(
                    data =
                        NationalRailStation(
                            name = "London Waterloo",
                            crsCode = "WAT",
                            stationAlerts =
                                listOf(
                                    NationalRailStationAlerts(
                                        alertText = "Temporary platform closure."
                                    )
                                ),
                        ),
                    meta = ResponseMeta(CacheStatus.MISS),
                )
                .toStationDetails()

        assertEquals(
            listOf(
                StationAlert(
                    title = null,
                    text = "Temporary platform closure.",
                    validFrom = null,
                    validTo = null,
                )
            ),
            details.alerts,
        )
    }

    @Test
    fun mapsAlertsAndPreservesHtmlContent() {
        val details = stationResponse().toStationDetails()

        assertEquals(
            listOf(
                StationAlert(
                    title = "Lift works",
                    text = "<p>Platform 1 lift <strong>out of service</strong> until Friday.</p>",
                    validFrom = "2026-07-01",
                    validTo = "2026-07-11",
                )
            ),
            details.alerts,
        )
        assertEquals(
            "<p>Cafe closed for <em>refurbishment</em></p>",
            details.facilities.first { it.name == "Refreshments" }.notes,
        )
    }

    @Test
    fun decodesPayAsYouGoContactlessObject() {
        val response =
            NetworkSerialization.json.decodeFromString<StationResponse>(
                """
                {
                  "data": {
                    "name": "London Waterloo",
                    "crsCode": "WAT",
                    "ticketBuying": {
                      "payAsYouGo": {
                        "contactless": {
                          "contactlessCards": false,
                          "notes": "<p>Contactless cards accepted</p>"
                        }
                      }
                    }
                  },
                  "meta": { "cacheStatus": "MISS" }
                }
                """
            )

        val details = response.toStationDetails()

        assertEquals(
            listOf(
                StationLabeledFact("Contactless", "No"),
                StationLabeledFact("Contactless notes", "<p>Contactless cards accepted</p>"),
            ),
            details.tickets?.facts,
        )
    }

    @Test
    fun mapsAccessibilityTicketsWaitingAndPlatforms() {
        val details = stationResponse().toStationDetails()

        assertEquals("A", details.accessibility?.stepFreeCategory)
        assertEquals("Step-free to all platforms", details.accessibility?.stepFreeNotes)
        assertEquals(true, details.accessibility?.wheelchairsAvailable)
        assertEquals("Yes on all platforms", details.accessibility?.tactilePaving)
        assertEquals(true, details.tickets?.ticketOfficeAvailable)
        assertEquals(true, details.tickets?.ticketMachinesAvailable)
        assertEquals("1", details.tickets?.londonFareZone)
        assertEquals(true, details.waiting?.shelteredWaitingAvailable)
        assertEquals("Waiting facility", details.waiting?.facilities?.first()?.name)
        assertEquals(24, details.platforms?.numberOfPlatforms)
        assertEquals("Platform 1", details.platforms?.platforms?.first()?.name)
    }

    @Test
    fun mapsFacilitiesTransportCarParksCyclingDropOffToiletsHelpAndLifts() {
        val details = stationResponse().toStationDetails()

        assertTrue(details.facilities.any { it.name == "WiFi" && it.available == true })
        assertTrue(details.facilities.any { it.name == "CCTV" && it.available == true })
        assertEquals(
            listOf(
                StationTransportLink(
                    name = "Bus",
                    available = true,
                    notes = "Outside main entrance",
                ),
                StationTransportLink(name = "Underground", available = false),
            ),
            details.transportLinks,
        )
        assertEquals(200, details.carParks?.numberOfSpaces)
        assertEquals("Main car park", details.carParks?.carParks?.first()?.name)
        assertEquals(
            listOf(StationLabeledFact("Daily", "£12")),
            details.carParks?.carParks?.first()?.charges,
        )
        assertEquals(true, details.cycling?.cycleStorageAvailable)
        assertEquals(40, details.cycling?.numberOfSpaces)
        assertEquals(listOf("Stands", "Lockers"), details.cycling?.typesOfStorage)
        assertEquals(true, details.dropOffPickUp?.available)
        assertEquals("Forecourt", details.dropOffPickUp?.points?.first()?.name)
        assertEquals(true, details.toilets?.available)
        assertEquals(true, details.toilets?.accessibleToiletsAvailable)
        assertEquals(true, details.help?.staffHelpAvailable)
        assertEquals(true, details.help?.helpPointsAvailable)
        assertEquals("Concourse", details.help?.helpPointsLocation)
        assertEquals(true, details.lifts?.available)
        assertEquals("Platform 1", details.lifts?.lifts?.first()?.name)
    }

    @Test
    fun omitsEmptyOptionalSections() {
        val details =
            StationResponse(
                    data = NationalRailStation(name = "Sparse Station", crsCode = "SPA"),
                    meta = ResponseMeta(CacheStatus.MISS),
                )
                .toStationDetails()

        assertNull(details.address)
        assertNull(details.location)
        assertNull(details.accessibility)
        assertNull(details.tickets)
        assertNull(details.waiting)
        assertNull(details.platforms)
        assertNull(details.carParks)
        assertNull(details.cycling)
        assertNull(details.dropOffPickUp)
        assertNull(details.toilets)
        assertNull(details.help)
        assertNull(details.lifts)
        assertTrue(details.alerts.isEmpty())
        assertTrue(details.facilities.isEmpty())
        assertTrue(details.transportLinks.isEmpty())
    }

    private fun stationResponse(): StationResponse =
        StationResponse(data = fixtureStation(), meta = ResponseMeta(CacheStatus.MISS))

    private fun fixtureStation(): NationalRailStation =
        NationalRailStation(
            name = "London Waterloo",
            crsCode = "WAT",
            staffingLevel = "Part Time",
            stationOperator = NationalRailStationOperator(name = "South Western Railway"),
            address =
                NationalRailAddress(
                    addressLine1 = "Station Approach",
                    addressLine2 = "London",
                    postcode = "SE1 8SW",
                ),
            location = NationalRailLocation(latitude = 51.5031, longitude = -0.1132),
            stationAlerts = listOf(fixtureAlert()),
            stationAccessibility = fixtureAccessibility(),
            stationFacilities = fixtureFacilities(),
            ticketBuying = fixtureTickets(),
            loungesAndWaiting = fixtureWaiting(),
            platformFacilities = fixturePlatforms(),
            transportLinks = fixtureTransport(),
            carParks = fixtureCarParks(),
            cycling = fixtureCycling(),
            dropOffPickUp = fixtureDropOff(),
            toiletsAndChanging = fixtureToilets(),
            helpAndSupport = fixtureHelp(),
            lifts = fixtureLifts(),
        )

    private fun fixtureAlert() =
        NationalRailStationAlerts(
            name = "alert-1",
            title = "Lift works",
            validFrom = "2026-07-01",
            validTo = "2026-07-11",
            alertText = "<p>Platform 1 lift <strong>out of service</strong> until Friday.</p>",
        )

    private fun fixtureAccessibility() =
        NationalRailStationAccessibility(
            stepFreeCategory =
                NationalRailStepFreeCategory(category = "A", notes = "Step-free to all platforms"),
            wheelchairsAvailable = true,
            tactilePaving = "Yes on all platforms",
        )

    private fun fixtureFacilities() =
        NationalRailStationFacilities(
            cctvAvailable = true,
            wifi = NationalRailStationFacility(available = true),
            refreshments =
                NationalRailStationFacility(
                    available = false,
                    notes = "<p>Cafe closed for <em>refurbishment</em></p>",
                ),
        )

    private fun fixtureTickets() =
        NationalRailTicketBuying(
            ticketOffice = NationalRailStationFacility(available = true),
            ticketMachinesAvailable = true,
            londonFareZone = "1",
        )

    private fun fixtureWaiting() =
        NationalRailLoungeAndWaiting(
            shelteredWaitingAvailable = true,
            waitingFacility = NationalRailStationFacility(available = true, location = "Concourse"),
        )

    private fun fixturePlatforms() =
        NationalRailPlatformFacilities(
            numberOfPlatforms = 24.0,
            platforms = listOf(NationalRailPlatform(name = "Platform 1", waitingType = "Seated")),
        )

    private fun fixtureTransport() =
        NationalRailTransportLinks(
            bus =
                NationalRailTransportLinkItem(
                    available = true,
                    notes = "Outside main entrance",
                ),
            underground = NationalRailTransportLinkItem(available = false),
        )

    private fun fixtureCarParks() =
        NationalRailStationCarParks(
            numberOfSpaces = 200.0,
            carParks =
                listOf(
                    NationalRailCarPark(
                        name = "Main car park",
                        numberOfSpaces = 200.0,
                        charges = NationalRailCarParkCharges(dailyRate = "£12"),
                    )
                ),
        )

    private fun fixtureCycling() =
        NationalRailCycling(
            cycleStorageAvailable = true,
            spaces = NationalRailCycleSpaces(numberOfSpaces = 40.0),
            typesOfStorage = listOf("Stands", "Lockers"),
            location = "Station approach",
        )

    private fun fixtureDropOff() =
        NationalRailDropOffPickUp(
            available = true,
            points = listOf(NationalRailDropOffPickUpPoint(name = "Forecourt")),
        )

    private fun fixtureToilets() =
        NationalRailToiletsAndChanging(
            toilets =
                NationalRailToilets(
                    available = true,
                    accessibleToiletsAvailable = true,
                    location = "Concourse",
                )
        )

    private fun fixtureHelp() =
        NationalRailHelpAndSupport(
            staffHelp = NationalRailStationFacility(available = true),
            helpPoints = NationalRailHelpPoints(available = true, location = "Concourse"),
        )

    private fun fixtureLifts() =
        NationalRailLifts(
            available = true,
            liftsInfo = listOf(NationalRailLift(name = "Platform 1")),
        )
}
