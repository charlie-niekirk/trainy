package me.cniekirk.trainy.core.data

import me.cniekirk.trainy.core.network.generated.model.NationalRailAddress
import me.cniekirk.trainy.core.network.generated.model.NationalRailCarPark
import me.cniekirk.trainy.core.network.generated.model.NationalRailCarParkCharges
import me.cniekirk.trainy.core.network.generated.model.NationalRailCycling
import me.cniekirk.trainy.core.network.generated.model.NationalRailDropOffPickUp
import me.cniekirk.trainy.core.network.generated.model.NationalRailDropOffPickUpPoint
import me.cniekirk.trainy.core.network.generated.model.NationalRailHelpAndSupport
import me.cniekirk.trainy.core.network.generated.model.NationalRailHelpPoints
import me.cniekirk.trainy.core.network.generated.model.NationalRailLift
import me.cniekirk.trainy.core.network.generated.model.NationalRailLifts
import me.cniekirk.trainy.core.network.generated.model.NationalRailLocation
import me.cniekirk.trainy.core.network.generated.model.NationalRailLoungeAndWaiting
import me.cniekirk.trainy.core.network.generated.model.NationalRailNamedFacility
import me.cniekirk.trainy.core.network.generated.model.NationalRailNearestStation
import me.cniekirk.trainy.core.network.generated.model.NationalRailPassengerAssistance
import me.cniekirk.trainy.core.network.generated.model.NationalRailPlatform
import me.cniekirk.trainy.core.network.generated.model.NationalRailPlatformFacilities
import me.cniekirk.trainy.core.network.generated.model.NationalRailStaffAssistance
import me.cniekirk.trainy.core.network.generated.model.NationalRailStation
import me.cniekirk.trainy.core.network.generated.model.NationalRailStationAccessibility
import me.cniekirk.trainy.core.network.generated.model.NationalRailStationCarParks
import me.cniekirk.trainy.core.network.generated.model.NationalRailStationFacilities
import me.cniekirk.trainy.core.network.generated.model.NationalRailStationFacility
import me.cniekirk.trainy.core.network.generated.model.NationalRailTicketBuying
import me.cniekirk.trainy.core.network.generated.model.NationalRailToiletsAndChanging
import me.cniekirk.trainy.core.network.generated.model.NationalRailTransportLinks
import me.cniekirk.trainy.core.network.generated.model.StationResponse

internal fun StationResponse.toStationDetails(): StationDetails = data.toStationDetails()

internal fun NationalRailStation.toStationDetails(): StationDetails =
    StationDetails(
        name = name,
        crsCode = crsCode,
        staffingLevel = staffingLevel?.takeIf(String::isNotBlank),
        operatorName = stationOperator?.name?.takeIf(String::isNotBlank),
        address = address?.toStationAddress(),
        location = location?.toStationCoordinates(),
        alerts =
            stationAlerts.orEmpty().map {
                StationAlert(
                    title = it.title?.takeIf(String::isNotBlank) ?: it.name,
                    text = it.alertText.stripHtml(),
                    validFrom = it.validFrom,
                    validTo = it.validTo?.takeIf(String::isNotBlank),
                )
            },
        accessibility = stationAccessibility?.toAccessibilitySection(),
        facilities = stationFacilities?.toFacilityItems().orEmpty(),
        tickets = ticketBuying?.toTicketsSection(),
        waiting = loungesAndWaiting?.toWaitingSection(),
        platforms = platformFacilities?.toPlatformsSection(),
        transportLinks = transportLinks?.toTransportLinks().orEmpty(),
        carParks = carParks?.toCarParksSection(),
        cycling = cycling?.toCyclingSection(),
        dropOffPickUp = dropOffPickUp?.toDropOffSection(),
        toilets = toiletsAndChanging?.toToiletsSection(),
        help = helpSection(helpAndSupport, staffAssistance),
        lifts = lifts?.toLiftsSection(),
    )

private fun NationalRailAddress.toStationAddress(): StationAddress? {
    val lines =
        listOfNotNull(
                addressLine1,
                addressLine2,
                addressLine3,
                addressLine4,
                addressLine5,
            )
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    val postcode = postcode?.takeIf(String::isNotBlank)
    return if (lines.isEmpty() && postcode == null) null
    else StationAddress(lines = lines, postcode = postcode)
}

private fun NationalRailLocation.toStationCoordinates(): StationCoordinates? {
    val lat = latitude
    val lng = longitude
    return if (lat != null && lng != null) StationCoordinates(latitude = lat, longitude = lng)
    else null
}

private fun NationalRailStationAccessibility.toAccessibilitySection():
    StationAccessibilitySection? {
    val section =
        StationAccessibilitySection(
            stepFreeCategory = stepFreeCategory?.category?.takeIf(String::isNotBlank),
            stepFreeNotes = stepFreeCategory?.notes?.stripHtml()?.takeIf(String::isNotBlank),
            wheelchairsAvailable = wheelchairsAvailable,
            tactilePaving = tactilePaving?.stripHtml()?.takeIf(String::isNotBlank),
            facts = accessibilityFacts(),
        )

    return section.takeUnless { it.isEmpty() }
}

private fun StationAccessibilitySection.isEmpty(): Boolean =
    listOf(stepFreeCategory, stepFreeNotes, wheelchairsAvailable, tactilePaving).all {
        it == null
    } && facts.isEmpty()

private fun NationalRailStationAccessibility.accessibilityFacts(): List<StationLabeledFact> =
    buildList {
        addOptionalFact("Ticket barriers", ticketBarriers?.available?.toYesNo())
        addOptionalFact(
            "Ticket barrier notes",
            ticketBarriers?.notes?.stripHtml()?.takeIf(String::isNotBlank),
        )
        addOptionalFact("Train ramp", trainRamp?.available?.toYesNo())
        addOptionalFact(
            "Induction loop",
            inductionLoop?.provision?.takeIf(String::isNotBlank)?.stripHtml(),
        )
        addOptionalFact(
            "Escalators",
            escalatorInformation?.takeIf(String::isNotBlank)?.stripHtml(),
        )
        addAll(passengerAssistance.orEmpty().mapNotNull { it.toAssistanceFact() })
        addAll(
            nearestAccessibleStations?.stations.orEmpty().mapNotNull {
                it.toNearestAccessibleFact()
            }
        )
        addOptionalFact(
            "Nearest accessible notes",
            nearestAccessibleStations?.notes?.stripHtml()?.takeIf(String::isNotBlank),
        )
    }

private fun NationalRailPassengerAssistance.toAssistanceFact(): StationLabeledFact? {
    val resolvedName = name?.takeIf(String::isNotBlank) ?: return null
    val value =
        listOfNotNull(
                available?.toYesNo(),
                location?.takeIf(String::isNotBlank),
                notes?.stripHtml()?.takeIf(String::isNotBlank),
            )
            .joinToString(" · ")
            .ifBlank { available?.toYesNo().orEmpty() }
    return value.takeIf(String::isNotBlank)?.let { StationLabeledFact(resolvedName, it) }
}

private fun NationalRailNearestStation.toNearestAccessibleFact(): StationLabeledFact? {
    val resolvedName = name?.takeIf(String::isNotBlank) ?: return null
    val value = listOfNotNull(crsCode, distance).filter { it.isNotBlank() }.joinToString(" · ")
    return if (value.isNotBlank()) {
        StationLabeledFact("Nearest accessible: $resolvedName", value)
    } else {
        StationLabeledFact("Nearest accessible", resolvedName)
    }
}

private fun MutableList<StationLabeledFact>.addOptionalFact(label: String, value: String?) {
    value?.takeIf(String::isNotBlank)?.let { add(StationLabeledFact(label, it)) }
}

private fun NationalRailStationFacilities.toFacilityItems(): List<StationFacilityItem> = buildList {
    addBooleanFacility("CCTV", cctvAvailable)
    addFacility("WiFi", wifi)
    addFacility("Refreshments", refreshments)
    addFacility("Shops", shops)
    addBooleanFacility("Pay phones", payPhones)
    addFacility("Luggage storage", luggageStorage)
    addFacility("Lost property", lostProperty)
    addFacility("Trolleys", trolleys)
    addFacility("ATM", atm)
    addFacility("Currency exchange", currencyExchange)
    addFacility("Post box", postBox)
    addFacility("Defibrillator", defibrillator)
    addFacility("Request stop", requestStop)
    addFacility("Limited service", limitedService)
}

private fun MutableList<StationFacilityItem>.addFacility(
    name: String,
    facility: NationalRailStationFacility?,
) {
    facility ?: return
    add(
        StationFacilityItem(
            name = name,
            available = facility.available,
            notes = facility.notes?.stripHtml()?.takeIf(String::isNotBlank),
            location = facility.location?.takeIf(String::isNotBlank),
        )
    )
}

private fun MutableList<StationFacilityItem>.addBooleanFacility(name: String, available: Boolean?) {
    available ?: return
    add(StationFacilityItem(name = name, available = available))
}

private fun NationalRailTicketBuying.toTicketsSection(): StationTicketsSection? {
    val facts = buildList {
        collectOnlineBookedTickets?.pickUpAtTicketOffice?.let {
            add(StationLabeledFact("Collect at ticket office", it.toYesNo()))
        }
        collectOnlineBookedTickets?.pickUpAtTicketMachine?.let {
            add(StationLabeledFact("Collect at ticket machine", it.toYesNo()))
        }
        payAsYouGo?.contactless?.let {
            add(StationLabeledFact("Contactless", it.toYesNo()))
        }
        payAsYouGo?.oyster?.purchaseOyster?.let {
            add(StationLabeledFact("Purchase Oyster", it.toYesNo()))
        }
        ticketMachines.orEmpty().forEach { machine ->
            val name = machine.name?.takeIf(String::isNotBlank) ?: return@forEach
            val value =
                listOfNotNull(
                        machine.isWheelchairHeight?.takeIf(String::isNotBlank),
                        machine.hasManoeuvringSpace?.takeIf(String::isNotBlank),
                    )
                    .joinToString(" · ")
            add(
                StationLabeledFact(
                    "Ticket machine: $name",
                    value.ifBlank { "Available" },
                )
            )
        }
    }

    val section =
        StationTicketsSection(
            ticketOfficeAvailable = ticketOffice?.available,
            ticketMachinesAvailable = ticketMachinesAvailable,
            londonFareZone = londonFareZone?.takeIf(String::isNotBlank),
            notes =
                listOfNotNull(
                        ticketSalesNotes?.stripHtml()?.takeIf(String::isNotBlank),
                        ticketOffice?.notes?.stripHtml()?.takeIf(String::isNotBlank),
                    )
                    .joinToString("\n")
                    .ifBlank { null },
            facts = facts,
        )

    return section.takeUnless { it.isEmpty() }
}

private fun StationTicketsSection.isEmpty(): Boolean =
    listOf(ticketOfficeAvailable, ticketMachinesAvailable, londonFareZone, notes).all {
        it == null
    } && facts.isEmpty()

private fun NationalRailLoungeAndWaiting.toWaitingSection(): StationWaitingSection? {
    val facilities = buildList {
        waitingFacility?.let {
            add(
                StationFacilityItem(
                    name = "Waiting facility",
                    available = it.available,
                    notes = it.notes?.stripHtml()?.takeIf(String::isNotBlank),
                    location = it.location?.takeIf(String::isNotBlank),
                )
            )
        }
        seatingArea?.let {
            add(
                StationFacilityItem(
                    name = "Seating area",
                    available = it.available,
                    notes = it.notes?.stripHtml()?.takeIf(String::isNotBlank),
                    location = it.location?.takeIf(String::isNotBlank),
                )
            )
        }
        firstClass?.let {
            add(
                StationFacilityItem(
                    name = "First class",
                    available = it.available,
                    notes = it.notes?.stripHtml()?.takeIf(String::isNotBlank),
                    location = it.location?.takeIf(String::isNotBlank),
                )
            )
        }
        waitingRooms.orEmpty().mapNotNullTo(this) { it.toFacilityItem("Waiting room") }
        firstClassLounges.orEmpty().mapNotNullTo(this) { it.toFacilityItem("First class lounge") }
    }

    val facts = buildList {
        quietRoom?.takeIf(String::isNotBlank)?.let {
            add(StationLabeledFact("Quiet room", it.stripHtml()))
        }
        faithRoom?.takeIf(String::isNotBlank)?.let {
            add(StationLabeledFact("Faith room", it.stripHtml()))
        }
    }

    val section =
        StationWaitingSection(
            shelteredWaitingAvailable = shelteredWaitingAvailable,
            facilities = facilities,
            facts = facts,
        )

    return section.takeUnless {
        it.shelteredWaitingAvailable == null && it.facilities.isEmpty() && it.facts.isEmpty()
    }
}

private fun NationalRailNamedFacility.toFacilityItem(fallbackName: String): StationFacilityItem? {
    if (!hasNamedFacilityContent()) return null
    return StationFacilityItem(
        name = name?.takeIf(String::isNotBlank) ?: fallbackName,
        available = available,
        notes = notes?.stripHtml()?.takeIf(String::isNotBlank),
        location = location?.takeIf(String::isNotBlank),
    )
}

private fun NationalRailNamedFacility.hasNamedFacilityContent(): Boolean =
    available != null || listOf(notes, location, name).any { !it.isNullOrBlank() }

private fun NationalRailPlatformFacilities.toPlatformsSection(): StationPlatformsSection? {
    val platforms = platforms.orEmpty().mapNotNull { it.toPlatformInfo() }
    val section =
        StationPlatformsSection(
            numberOfPlatforms = numberOfPlatforms?.toInt(),
            entranceLevels = entranceLevels?.stripHtml()?.takeIf(String::isNotBlank),
            tactileWarnings = tactileWarnings?.stripHtml()?.takeIf(String::isNotBlank),
            platforms = platforms,
        )
    return section.takeUnless { it.isEmpty() }
}

private fun StationPlatformsSection.isEmpty(): Boolean =
    listOf(numberOfPlatforms, entranceLevels, tactileWarnings).all { it == null } &&
        platforms.isEmpty()

private fun NationalRailPlatform.toPlatformInfo(): StationPlatformInfo? {
    val resolvedName = name?.takeIf(String::isNotBlank) ?: return null
    return StationPlatformInfo(
        name = resolvedName,
        waitingType = waitingType?.takeIf(String::isNotBlank),
        seatingAtIntervals = seatingAtIntervals?.takeIf(String::isNotBlank),
        helpPointClose = helpPointClose?.takeIf(String::isNotBlank),
    )
}

private fun NationalRailTransportLinks.toTransportLinks(): List<StationTransportLink> = buildList {
    replacementBus?.let {
        add(
            StationTransportLink(
                name = "Replacement bus",
                available = it.available,
                notes = it.notes?.stripHtml()?.takeIf(String::isNotBlank),
                location = it.location?.takeIf(String::isNotBlank),
            )
        )
    }
    bus?.let {
        add(
            StationTransportLink(
                name = "Bus",
                available = it.available,
                notes = it.notes?.stripHtml()?.takeIf(String::isNotBlank),
            )
        )
    }
    underground?.let {
        add(
            StationTransportLink(
                name = "Underground",
                available = it.available,
                notes = it.notes?.stripHtml()?.takeIf(String::isNotBlank),
            )
        )
    }
    airport?.let {
        add(
            StationTransportLink(
                name = "Airport",
                available = it.available,
                notes = it.notes?.stripHtml()?.takeIf(String::isNotBlank),
            )
        )
    }
    port?.let {
        add(
            StationTransportLink(
                name = "Port",
                available = it.available,
                notes = it.notes?.stripHtml()?.takeIf(String::isNotBlank),
            )
        )
    }
    carHire?.let {
        add(
            StationTransportLink(
                name = "Car hire",
                available = it.available,
                notes = it.notes?.stripHtml()?.takeIf(String::isNotBlank),
            )
        )
    }
    taxi?.let {
        add(
            StationTransportLink(
                name = "Taxi",
                available = it.available,
                notes = it.notes?.stripHtml()?.takeIf(String::isNotBlank),
            )
        )
    }
}

private fun NationalRailStationCarParks.toCarParksSection(): StationCarParksSection? {
    val parks = carParks.orEmpty().mapNotNull { it.toCarParkInfo() }
    val section =
        StationCarParksSection(
            numberOfSpaces = numberOfSpaces?.toInt(),
            numberOfAccessibleSpaces = numberOfAccessibleSpaces?.toInt(),
            parkingSpacesAvailable = parkingSpacesAvailable,
            carParks = parks,
        )
    return section.takeUnless { it.isEmpty() }
}

private fun StationCarParksSection.isEmpty(): Boolean =
    listOf(numberOfSpaces, numberOfAccessibleSpaces, parkingSpacesAvailable).all { it == null } &&
        carParks.isEmpty()

private fun NationalRailCarPark.toCarParkInfo(): StationCarParkInfo? {
    val charges = charges?.toFacts().orEmpty()
    if (!hasCarParkContent(charges)) return null
    return StationCarParkInfo(
        name = name?.takeIf(String::isNotBlank) ?: "Car park",
        numberOfSpaces = numberOfSpaces?.toInt(),
        numberOfAccessibleSpaces = numberOfAccessibleSpaces?.toInt(),
        freeParking = freeParking,
        cctv = cctv,
        notes = notes?.stripHtml()?.takeIf(String::isNotBlank),
        charges = charges,
    )
}

private fun NationalRailCarPark.hasCarParkContent(charges: List<StationLabeledFact>): Boolean =
    listOf(name, notes).any { !it.isNullOrBlank() } ||
        listOf(numberOfSpaces, numberOfAccessibleSpaces, freeParking, cctv).any { it != null } ||
        charges.isNotEmpty()

private fun NationalRailCarParkCharges.toFacts(): List<StationLabeledFact> = buildList {
    perHourRate?.takeIf(String::isNotBlank)?.let { add(StationLabeledFact("Per hour", it)) }
    offPeakRate?.takeIf(String::isNotBlank)?.let { add(StationLabeledFact("Off-peak", it)) }
    dailyRate?.takeIf(String::isNotBlank)?.let { add(StationLabeledFact("Daily", it)) }
    saturdayRate?.takeIf(String::isNotBlank)?.let { add(StationLabeledFact("Saturday", it)) }
    sundayRate?.takeIf(String::isNotBlank)?.let { add(StationLabeledFact("Sunday", it)) }
    weeklyRate?.takeIf(String::isNotBlank)?.let { add(StationLabeledFact("Weekly", it)) }
    monthlyRate?.takeIf(String::isNotBlank)?.let { add(StationLabeledFact("Monthly", it)) }
    threeMonthlyRate?.takeIf(String::isNotBlank)?.let {
        add(StationLabeledFact("Three-monthly", it))
    }
    sixMonthlyRate?.takeIf(String::isNotBlank)?.let {
        add(StationLabeledFact("Six-monthly", it))
    }
    annualRate?.takeIf(String::isNotBlank)?.let { add(StationLabeledFact("Annual", it)) }
}

private fun NationalRailCycling.toCyclingSection(): StationCyclingSection? {
    val section =
        StationCyclingSection(
            cycleStorageAvailable = cycleStorageAvailable,
            numberOfSpaces = spaces?.numberOfSpaces?.toInt(),
            location = location?.takeIf(String::isNotBlank),
            sheltered = sheltered,
            cctv = cctv,
            typesOfStorage = typesOfStorage.orEmpty().filter { it.isNotBlank() },
            notes =
                listOfNotNull(
                        spaces?.notes?.stripHtml()?.takeIf(String::isNotBlank),
                        cycleHireNotes?.stripHtml()?.takeIf(String::isNotBlank),
                    )
                    .joinToString("\n")
                    .ifBlank { null },
        )
    return section.takeUnless { it.isEmpty() }
}

private fun StationCyclingSection.isEmpty(): Boolean =
    listOf(cycleStorageAvailable, numberOfSpaces, location, sheltered, cctv, notes).all {
        it == null
    } && typesOfStorage.isEmpty()

private fun NationalRailDropOffPickUp.toDropOffSection(): StationDropOffSection? {
    val points = points.orEmpty().mapNotNull { it.toDropOffPoint() }
    val section =
        StationDropOffSection(
            available = available,
            location = location?.takeIf(String::isNotBlank),
            notes = notes?.stripHtml()?.takeIf(String::isNotBlank),
            points = points,
        )
    return section.takeUnless { it.isEmpty() }
}

private fun StationDropOffSection.isEmpty(): Boolean =
    listOf(available, location, notes).all { it == null } && points.isEmpty()

private fun NationalRailDropOffPickUpPoint.toDropOffPoint(): StationDropOffPoint? {
    val resolvedName = name?.takeIf(String::isNotBlank) ?: return null
    val facts = buildList {
        sameSideAsStation?.takeIf(String::isNotBlank)?.let {
            add(StationLabeledFact("Same side as station", it))
        }
        waitingAreaForDisabledPickup?.takeIf(String::isNotBlank)?.let {
            add(StationLabeledFact("Accessible waiting area", it))
        }
        typeOfFacility?.takeIf(String::isNotBlank)?.let {
            add(StationLabeledFact("Facility type", it))
        }
        carAccommodationCount?.takeIf(String::isNotBlank)?.let {
            add(StationLabeledFact("Car spaces", it))
        }
        easyToDeployRamp?.takeIf(String::isNotBlank)?.let {
            add(StationLabeledFact("Easy to deploy ramp", it))
        }
        helpPointClose?.takeIf(String::isNotBlank)?.let {
            add(StationLabeledFact("Help point close", it))
        }
    }
    return StationDropOffPoint(name = resolvedName, facts = facts)
}

private fun NationalRailToiletsAndChanging.toToiletsSection(): StationToiletsSection? {
    val toilets = toilets ?: return null
    val section =
        StationToiletsSection(
            available = toilets.available,
            accessibleToiletsAvailable = toilets.accessibleToiletsAvailable,
            changingPlacesAvailable = toilets.changingPlacesToiletsAvailable,
            location = toilets.location?.takeIf(String::isNotBlank),
            notes = toilets.notes?.stripHtml()?.takeIf(String::isNotBlank),
        )
    return section.takeUnless { it.isEmpty() }
}

private fun StationToiletsSection.isEmpty(): Boolean =
    listOf(available, accessibleToiletsAvailable, changingPlacesAvailable, location, notes).all {
        it == null
    }

private fun helpSection(
    helpAndSupport: NationalRailHelpAndSupport?,
    staffAssistance: NationalRailStaffAssistance?,
): StationHelpSection? {
    val helpPoints = helpAndSupport?.helpPoints ?: staffAssistance?.helpPoints
    val section =
        StationHelpSection(
            staffHelpAvailable =
                helpAndSupport?.staffHelp?.available ?: staffAssistance?.staffHelp?.available,
            helpPointsAvailable = helpPoints?.available,
            helpPointsLocation = helpPoints?.location?.takeIf(String::isNotBlank),
            announcements =
                helpAndSupport?.announcements?.stripHtml()?.takeIf(String::isNotBlank)
                    ?: staffAssistance?.announcements?.stripHtml()?.takeIf(String::isNotBlank),
            facts = helpFacts(helpAndSupport, staffAssistance, helpPoints),
        )

    return section.takeUnless { it.isEmpty() }
}

private fun StationHelpSection.isEmpty(): Boolean =
    listOf(staffHelpAvailable, helpPointsAvailable, helpPointsLocation, announcements).all {
        it == null
    } && facts.isEmpty()

private fun helpFacts(
    helpAndSupport: NationalRailHelpAndSupport?,
    staffAssistance: NationalRailStaffAssistance?,
    helpPoints: NationalRailHelpPoints?,
): List<StationLabeledFact> = buildList {
    addOptionalFact(
        "Induction loops",
        helpAndSupport?.inductionLoops?.takeIf(String::isNotBlank)?.stripHtml(),
    )
    addOptionalFact(
        "Printed local information",
        helpAndSupport?.printedLocalInformation?.takeIf(String::isNotBlank)?.stripHtml(),
    )
    addOptionalFact(
        "Information points",
        helpAndSupport?.informationPoints?.takeIf(String::isNotBlank)?.stripHtml(),
    )
    addOptionalFact(
        "Accessibility information",
        helpAndSupport?.accessibilityInformation?.takeIf(String::isNotBlank)?.stripHtml(),
    )
    addJoinedFact(
        "Customer information screens",
        helpAndSupport?.customerInformationScreens.orEmpty(),
    )
    addJoinedFact("Staff information", helpAndSupport?.staffInformation.orEmpty())
    addJoinedFact(
        "Information available from staff",
        staffAssistance?.informationAvailableFromStaff.orEmpty(),
    )
    staffAssistance?.helpline?.let { helpline ->
        val value =
            listOfNotNull(
                    helpline.available?.toYesNo(),
                    helpline.notes?.stripHtml()?.takeIf(String::isNotBlank),
                )
                .joinToString(" · ")
        addOptionalFact("Helpline", value)
    }
    addOptionalFact("Help point notes", helpPoints?.notes?.stripHtml()?.takeIf(String::isNotBlank))
    addOptionalFact(
        "Help point induction loop",
        helpPoints?.inductionLoop?.takeIf(String::isNotBlank)?.stripHtml(),
    )
}

private fun MutableList<StationLabeledFact>.addJoinedFact(label: String, values: List<String>) {
    values
        .filter { it.isNotBlank() }
        .takeIf { it.isNotEmpty() }
        ?.let { add(StationLabeledFact(label, it.joinToString(", "))) }
}

private fun NationalRailLifts.toLiftsSection(): StationLiftsSection? {
    val lifts = liftsInfo.orEmpty().mapNotNull { it.toLiftInfo() }
    val section =
        StationLiftsSection(
            available = available,
            statement = statement?.stripHtml()?.takeIf(String::isNotBlank),
            lifts = lifts,
        )
    return section.takeUnless {
        it.available == null && it.statement == null && it.lifts.isEmpty()
    }
}

private fun NationalRailLift.toLiftInfo(): StationLiftInfo? {
    val resolvedName = name?.takeIf(String::isNotBlank) ?: return null
    val facts = buildList {
        operationalAnnouncements?.takeIf(String::isNotBlank)?.let {
            add(StationLabeledFact("Announcements", it.stripHtml()))
        }
        liftControls?.takeIf(String::isNotBlank)?.let {
            add(StationLabeledFact("Controls", it.stripHtml()))
        }
        liftManoeuvrability?.takeIf(String::isNotBlank)?.let {
            add(StationLabeledFact("Manoeuvrability", it.stripHtml()))
        }
        openingWidth?.takeIf(String::isNotBlank)?.let {
            add(StationLabeledFact("Opening width", it.stripHtml()))
        }
    }
    return StationLiftInfo(name = resolvedName, facts = facts)
}

private fun Boolean.toYesNo(): String = if (this) "Yes" else "No"

internal fun String.stripHtml(): String =
    replace(HTML_TAG_REGEX, " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .trim()
        .replace(WHITESPACE_REGEX, " ")

private val HTML_TAG_REGEX = Regex("<[^>]+>")
private val WHITESPACE_REGEX = Regex("\\s+")
