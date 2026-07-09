package me.cniekirk.trainy.core.network.generated.model

import kotlinx.serialization.Serializable

@Serializable data class StationResponse(val data: NationalRailStation, val meta: ResponseMeta)

@Serializable
data class NationalRailStation(
    val name: String,
    val slug: String? = null,
    val sixteenCharacterName: String? = null,
    val crsCode: String,
    val nationalLocationCode: String? = null,
    val minimumConnectionTime: Double? = null,
    val address: NationalRailAddress? = null,
    val location: NationalRailLocation? = null,
    val stationAlerts: List<NationalRailStationAlerts>? = null,
    val stationOperator: NationalRailStationOperator? = null,
    val stationMap: NationalRailAsset? = null,
    val staffingLevel: String? = null,
    val toiletsAndChanging: NationalRailToiletsAndChanging? = null,
    val stationAccessibility: NationalRailStationAccessibility? = null,
    val staffAssistance: NationalRailStaffAssistance? = null,
    val transportLinks: NationalRailTransportLinks? = null,
    val lifts: NationalRailLifts? = null,
    val ticketBuying: NationalRailTicketBuying? = null,
    val loungesAndWaiting: NationalRailLoungeAndWaiting? = null,
    val stationFacilities: NationalRailStationFacilities? = null,
    val helpAndSupport: NationalRailHelpAndSupport? = null,
    val platformFacilities: NationalRailPlatformFacilities? = null,
    val cycling: NationalRailCycling? = null,
    val dropOffPickUp: NationalRailDropOffPickUp? = null,
    val carParks: NationalRailStationCarParks? = null,
)

@Serializable
data class NationalRailStationOperator(
    val name: String? = null,
    val code: String? = null,
)

@Serializable
data class NationalRailAddress(
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val addressLine3: String? = null,
    val addressLine4: String? = null,
    val addressLine5: String? = null,
    val postcode: String? = null,
)

@Serializable
data class NationalRailLocation(val latitude: Double? = null, val longitude: Double? = null)

@Serializable
data class NationalRailStationAlerts(
    val name: String,
    val title: String? = null,
    val validFrom: String,
    val validTo: String? = null,
    val alertText: String,
)

@Serializable data class NationalRailAsset(val name: String? = null, val url: String? = null)

@Serializable
data class NationalRailStationFacility(
    val available: Boolean? = null,
    val openingTimes: List<NationalRailOpeningTimes>? = null,
    val openingHoursNotes: String? = null,
    val operatorContactDetails: NationalRailFacilityContactDetails? = null,
    val notes: String? = null,
    val location: String? = null,
)

@Serializable
data class NationalRailOpeningTimes(
    val daysOfTheWeek: List<String>? = null,
    val openingStatus: String? = null,
    val openPeriod: List<NationalRailOpenPeriod>? = null,
)

@Serializable
data class NationalRailOpenPeriod(val startTime: String? = null, val endTime: String? = null)

@Serializable
data class NationalRailFacilityContactDetails(
    val primaryTelephoneNumber: String? = null,
    val emailAddress: String? = null,
    val operatorName: String? = null,
    val note: String? = null,
)

@Serializable
data class NationalRailToiletsAndChanging(
    val toilets: NationalRailToilets? = null,
    val showers: NationalRailStationFacility? = null,
)

@Serializable
data class NationalRailToilets(
    val available: Boolean? = null,
    val openingTimes: List<NationalRailOpeningTimes>? = null,
    val openingHoursNotes: String? = null,
    val operatorContactDetails: NationalRailFacilityContactDetails? = null,
    val notes: String? = null,
    val location: String? = null,
    val accessibleToiletsAvailable: Boolean? = null,
    val changingPlacesToiletsAvailable: Boolean? = null,
)

@Serializable
data class NationalRailStationAccessibility(
    val stepFreeCategory: NationalRailStepFreeCategory? = null,
    val nearestAccessibleStations: NationalRailNearestAccessibleStations? = null,
    val ticketBarriers: NationalRailTicketBarriers? = null,
    val trainRamp: NationalRailTrainRamp? = null,
    val tactilePaving: String? = null,
    val inductionLoop: NationalRailInductionLoop? = null,
    val wheelchairsAvailable: Boolean? = null,
    val escalatorInformation: String? = null,
    val passengerAssistance: List<NationalRailPassengerAssistance>? = null,
)

@Serializable
data class NationalRailStepFreeCategory(
    val category: String? = null,
    val notes: String? = null,
)

@Serializable
data class NationalRailNearestAccessibleStations(
    val stations: List<NationalRailNearestStation>? = null,
    val notes: String? = null,
)

@Serializable
data class NationalRailNearestStation(
    val name: String? = null,
    val crsCode: String? = null,
    val distance: String? = null,
)

@Serializable
data class NationalRailTicketBarriers(
    val available: Boolean? = null,
    val openingTimes: List<NationalRailOpeningTimes>? = null,
    val openingHoursNotes: String? = null,
    val operatorContactDetails: NationalRailFacilityContactDetails? = null,
    val notes: String? = null,
    val location: String? = null,
    val names: List<String>? = null,
)

@Serializable
data class NationalRailTrainRamp(
    val available: Boolean? = null,
    val openingTimes: List<NationalRailOpeningTimes>? = null,
    val openingHoursNotes: String? = null,
    val operatorContactDetails: NationalRailFacilityContactDetails? = null,
    val notes: String? = null,
    val location: String? = null,
    val storage: String? = null,
)

@Serializable
data class NationalRailInductionLoop(
    val provision: String? = null,
    val ticketCounters: Boolean? = null,
)

@Serializable
data class NationalRailPassengerAssistance(
    val name: String? = null,
    val available: Boolean? = null,
    val location: String? = null,
    val notes: String? = null,
)

@Serializable
data class NationalRailStaffAssistance(
    val staffHelp: NationalRailStationFacility? = null,
    val helpline: NationalRailStationFacility? = null,
    val helpPoints: NationalRailHelpPoints? = null,
    val informationAvailableFromStaff: List<String>? = null,
    val customerInformation: List<String>? = null,
    val announcements: String? = null,
    val customerServiceNotes: String? = null,
)

@Serializable
data class NationalRailHelpPoints(
    val available: Boolean? = null,
    val location: String? = null,
    val notes: String? = null,
    val inductionLoop: String? = null,
)

@Serializable
data class NationalRailTransportLinks(
    val replacementBus: NationalRailReplacementBusTransportLinkItem? = null,
    val bus: NationalRailTransportLinkItem? = null,
    val underground: NationalRailTransportLinkItem? = null,
    val airport: NationalRailTransportLinkItem? = null,
    val port: NationalRailTransportLinkItem? = null,
    val carHire: NationalRailTransportLinkItem? = null,
    val taxi: NationalRailTaxi? = null,
)

@Serializable
data class NationalRailTransportLinkItem(
    val available: Boolean? = null,
    val notes: String? = null,
)

@Serializable
data class NationalRailReplacementBusTransportLinkItem(
    val available: Boolean? = null,
    val notes: String? = null,
    val location: String? = null,
)

@Serializable
data class NationalRailTaxi(
    val available: Boolean? = null,
    val notes: String? = null,
    val taxiRanks: List<NationalRailTaxiType>? = null,
)

@Serializable
data class NationalRailTaxiType(
    val name: String? = null,
    val taxiConfiguration: String? = null,
    val localityOnSameSide: String? = null,
    val covered: String? = null,
    val deployableRamp: String? = null,
    val helpPoint: String? = null,
)

@Serializable
data class NationalRailLifts(
    val available: Boolean? = null,
    val statement: String? = null,
    val liftsInfo: List<NationalRailLift>? = null,
)

@Serializable
data class NationalRailLift(
    val name: String? = null,
    val operationalAnnouncements: String? = null,
    val liftControls: String? = null,
    val liftManoeuvrability: String? = null,
    val openingWidth: String? = null,
)

@Serializable
data class NationalRailTicketBuying(
    val ticketOffice: NationalRailStationFacility? = null,
    val ticketSalesNotes: String? = null,
    val ticketMachinesAvailable: Boolean? = null,
    val vending: List<NationalRailTicketVending>? = null,
    val travelCentres: List<NationalRailTravelCentre>? = null,
    val ticketMachines: List<NationalRailTicketMachine>? = null,
    val collectOnlineBookedTickets: NationalRailCollectOnlineBookedTickets? = null,
    val londonFareZone: String? = null,
    val payAsYouGo: NationalRailPayAsYouGo? = null,
)

@Serializable
data class NationalRailTicketVending(
    val name: String? = null,
    val countersAvailable: String? = null,
    val countersWheelchairAccessible: String? = null,
)

@Serializable
data class NationalRailTravelCentre(
    val name: String? = null,
    val countersOrVendingAvailable: String? = null,
    val waitingAvailable: String? = null,
)

@Serializable
data class NationalRailTicketMachine(
    val name: String? = null,
    val hasManoeuvringSpace: String? = null,
    val isWheelchairHeight: String? = null,
)

@Serializable
data class NationalRailCollectOnlineBookedTickets(
    val pickUpAtTicketOffice: Boolean? = null,
    val pickUpAtTicketMachine: Boolean? = null,
)

@Serializable
data class NationalRailPayAsYouGo(
    val oyster: NationalRailOyster? = null,
    val contactless: Boolean? = null,
)

@Serializable
data class NationalRailOyster(
    val purchaseOyster: Boolean? = null,
    val topUpOyster: NationalRailTopUpOyster? = null,
)

@Serializable
data class NationalRailTopUpOyster(
    val atTicketOffice: Boolean? = null,
    val atTicketMachine: Boolean? = null,
)

@Serializable
data class NationalRailLoungeAndWaiting(
    val shelteredWaitingAvailable: Boolean? = null,
    val waitingFacility: NationalRailStationFacility? = null,
    val seatingArea: NationalRailStationFacility? = null,
    val firstClass: NationalRailStationFacility? = null,
    val waitingRooms: List<NationalRailNamedFacility>? = null,
    val firstClassLounges: List<NationalRailNamedFacility>? = null,
    val quietRoom: String? = null,
    val faithRoom: String? = null,
)

@Serializable
data class NationalRailNamedFacility(
    val name: String? = null,
    val available: Boolean? = null,
    val location: String? = null,
    val notes: String? = null,
)

@Serializable
data class NationalRailStationFacilities(
    val cctvAvailable: Boolean? = null,
    val wifi: NationalRailStationFacility? = null,
    val refreshments: NationalRailStationFacility? = null,
    val shops: NationalRailStationFacility? = null,
    val payPhones: Boolean? = null,
    val luggageStorage: NationalRailStationFacility? = null,
    val lostProperty: NationalRailStationFacility? = null,
    val trolleys: NationalRailStationFacility? = null,
    val atm: NationalRailStationFacility? = null,
    val currencyExchange: NationalRailStationFacility? = null,
    val postBox: NationalRailStationFacility? = null,
    val defibrillator: NationalRailStationFacility? = null,
    val requestStop: NationalRailStationFacility? = null,
    val limitedService: NationalRailStationFacility? = null,
)

@Serializable
data class NationalRailHelpAndSupport(
    val helpPoints: NationalRailHelpPoints? = null,
    val announcements: String? = null,
    val inductionLoops: String? = null,
    val printedLocalInformation: String? = null,
    val customerInformationScreens: List<String>? = null,
    val staffInformation: List<String>? = null,
    val informationPoints: String? = null,
    val accessibilityInformation: String? = null,
    val staffHelp: NationalRailStationFacility? = null,
)

@Serializable
data class NationalRailPlatformFacilities(
    val entranceLevels: String? = null,
    val tactileWarnings: String? = null,
    val numberOfPlatforms: Double? = null,
    val platforms: List<NationalRailPlatform>? = null,
)

@Serializable
data class NationalRailPlatform(
    val name: String? = null,
    val waitingType: String? = null,
    val seatingAtIntervals: String? = null,
    val helpPointClose: String? = null,
)

@Serializable
data class NationalRailCycling(
    val cycleStorageAvailable: Boolean? = null,
    val cycleHireNotes: String? = null,
    val spaces: NationalRailCycleSpaces? = null,
    val typesOfStorage: List<String>? = null,
    val location: String? = null,
    val sheltered: Boolean? = null,
    val cctv: Boolean? = null,
)

@Serializable
data class NationalRailCycleSpaces(
    val numberOfSpaces: Double? = null,
    val notes: String? = null,
)

@Serializable
data class NationalRailDropOffPickUp(
    val available: Boolean? = null,
    val notes: String? = null,
    val location: String? = null,
    val points: List<NationalRailDropOffPickUpPoint>? = null,
)

@Serializable
data class NationalRailDropOffPickUpPoint(
    val name: String? = null,
    val sameSideAsStation: String? = null,
    val waitingAreaForDisabledPickup: String? = null,
    val typeOfFacility: String? = null,
    val carAccommodationCount: String? = null,
    val easyToDeployRamp: String? = null,
    val helpPointClose: String? = null,
)

@Serializable
data class NationalRailStationCarParks(
    val numberOfSpaces: Double? = null,
    val parkingSpacesAvailable: Boolean? = null,
    val numberOfAccessibleSpaces: Double? = null,
    val accessibleParkingSpacesAvailable: Boolean? = null,
    val carParks: List<NationalRailCarPark>? = null,
)

@Serializable
data class NationalRailCarPark(
    val name: String? = null,
    val operator: NationalRailCarParkOperator? = null,
    val openingHours: List<NationalRailOpeningTimes>? = null,
    val numberOfSpaces: Double? = null,
    val cctv: Boolean? = null,
    val freeParking: Boolean? = null,
    val charges: NationalRailCarParkCharges? = null,
    val notes: String? = null,
    val numberOfAccessibleSpaces: Double? = null,
    val accessibleSpacesNotes: String? = null,
    val accessibleLocations: List<NationalRailCarParkAccessibilityLocation>? = null,
)

@Serializable
data class NationalRailCarParkOperator(val contactDetails: NationalRailContactDetails? = null)

@Serializable
data class NationalRailContactDetails(
    val primaryTelephoneNumber: String? = null,
    val emailAddress: String? = null,
    val operatorName: String? = null,
    val note: String? = null,
)

@Serializable
data class NationalRailCarParkCharges(
    val perHourRate: String? = null,
    val offPeakRate: String? = null,
    val dailyRate: String? = null,
    val saturdayRate: String? = null,
    val sundayRate: String? = null,
    val weeklyRate: String? = null,
    val monthlyRate: String? = null,
    val threeMonthlyRate: String? = null,
    val sixMonthlyRate: String? = null,
    val annualRate: String? = null,
)

@Serializable
data class NationalRailCarParkAccessibilityLocation(
    val name: String? = null,
    val accessibilityInfo: NationalRailCarParkAccessibilityInfo? = null,
)

@Serializable
data class NationalRailCarParkAccessibilityInfo(
    val accessibleParkingSpaces: String? = null,
    val location: String? = null,
    val parkingDuration: String? = null,
    val sizeOfOffRoadBays: String? = null,
    val sizeOfRoadBays: String? = null,
    val evChargingAccessible: String? = null,
    val evChargingNonAccessible: String? = null,
    val rampDeployable: String? = null,
    val helpPointClose: String? = null,
)
