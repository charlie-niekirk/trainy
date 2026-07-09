package me.cniekirk.trainy.core.data

data class StationDetails(
    val name: String,
    val crsCode: String,
    val staffingLevel: String? = null,
    val operatorName: String? = null,
    val address: StationAddress? = null,
    val location: StationCoordinates? = null,
    val alerts: List<StationAlert> = emptyList(),
    val accessibility: StationAccessibilitySection? = null,
    val facilities: List<StationFacilityItem> = emptyList(),
    val tickets: StationTicketsSection? = null,
    val waiting: StationWaitingSection? = null,
    val platforms: StationPlatformsSection? = null,
    val transportLinks: List<StationTransportLink> = emptyList(),
    val carParks: StationCarParksSection? = null,
    val cycling: StationCyclingSection? = null,
    val dropOffPickUp: StationDropOffSection? = null,
    val toilets: StationToiletsSection? = null,
    val help: StationHelpSection? = null,
    val lifts: StationLiftsSection? = null,
)

data class StationAddress(
    val lines: List<String>,
    val postcode: String?,
)

data class StationCoordinates(
    val latitude: Double,
    val longitude: Double,
)

data class StationAlert(
    val title: String? = null,
    val text: String,
    val validFrom: String? = null,
    val validTo: String? = null,
)

data class StationFacilityItem(
    val name: String,
    val available: Boolean? = null,
    val notes: String? = null,
    val location: String? = null,
)

data class StationLabeledFact(
    val label: String,
    val value: String,
)

data class StationAccessibilitySection(
    val stepFreeCategory: String? = null,
    val stepFreeNotes: String? = null,
    val wheelchairsAvailable: Boolean? = null,
    val tactilePaving: String? = null,
    val facts: List<StationLabeledFact> = emptyList(),
)

data class StationTicketsSection(
    val ticketOfficeAvailable: Boolean? = null,
    val ticketMachinesAvailable: Boolean? = null,
    val londonFareZone: String? = null,
    val notes: String? = null,
    val facts: List<StationLabeledFact> = emptyList(),
)

data class StationWaitingSection(
    val shelteredWaitingAvailable: Boolean? = null,
    val facilities: List<StationFacilityItem> = emptyList(),
    val facts: List<StationLabeledFact> = emptyList(),
)

data class StationPlatformsSection(
    val numberOfPlatforms: Int? = null,
    val entranceLevels: String? = null,
    val tactileWarnings: String? = null,
    val platforms: List<StationPlatformInfo> = emptyList(),
)

data class StationPlatformInfo(
    val name: String,
    val waitingType: String? = null,
    val seatingAtIntervals: String? = null,
    val helpPointClose: String? = null,
)

data class StationTransportLink(
    val name: String,
    val available: Boolean? = null,
    val notes: String? = null,
    val location: String? = null,
)

data class StationCarParksSection(
    val numberOfSpaces: Int? = null,
    val numberOfAccessibleSpaces: Int? = null,
    val parkingSpacesAvailable: Boolean? = null,
    val carParks: List<StationCarParkInfo> = emptyList(),
)

data class StationCarParkInfo(
    val name: String,
    val numberOfSpaces: Int? = null,
    val numberOfAccessibleSpaces: Int? = null,
    val freeParking: Boolean? = null,
    val cctv: Boolean? = null,
    val notes: String? = null,
    val charges: List<StationLabeledFact> = emptyList(),
)

data class StationCyclingSection(
    val cycleStorageAvailable: Boolean? = null,
    val numberOfSpaces: Int? = null,
    val location: String? = null,
    val sheltered: Boolean? = null,
    val cctv: Boolean? = null,
    val typesOfStorage: List<String> = emptyList(),
    val notes: String? = null,
)

data class StationDropOffSection(
    val available: Boolean? = null,
    val location: String? = null,
    val notes: String? = null,
    val points: List<StationDropOffPoint> = emptyList(),
)

data class StationDropOffPoint(
    val name: String,
    val facts: List<StationLabeledFact> = emptyList(),
)

data class StationToiletsSection(
    val available: Boolean? = null,
    val accessibleToiletsAvailable: Boolean? = null,
    val changingPlacesAvailable: Boolean? = null,
    val location: String? = null,
    val notes: String? = null,
)

data class StationHelpSection(
    val staffHelpAvailable: Boolean? = null,
    val helpPointsAvailable: Boolean? = null,
    val helpPointsLocation: String? = null,
    val announcements: String? = null,
    val facts: List<StationLabeledFact> = emptyList(),
)

data class StationLiftsSection(
    val available: Boolean? = null,
    val statement: String? = null,
    val lifts: List<StationLiftInfo> = emptyList(),
)

data class StationLiftInfo(
    val name: String,
    val facts: List<StationLabeledFact> = emptyList(),
)
