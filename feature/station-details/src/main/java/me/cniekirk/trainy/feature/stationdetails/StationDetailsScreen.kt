package me.cniekirk.trainy.feature.stationdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.zacsweers.metrox.viewmodel.metroViewModel
import me.cniekirk.trainy.core.data.StationAccessibilitySection
import me.cniekirk.trainy.core.data.StationAddress
import me.cniekirk.trainy.core.data.StationAlert
import me.cniekirk.trainy.core.data.StationCarParkInfo
import me.cniekirk.trainy.core.data.StationCarParksSection
import me.cniekirk.trainy.core.data.StationCoordinates
import me.cniekirk.trainy.core.data.StationCyclingSection
import me.cniekirk.trainy.core.data.StationDetails
import me.cniekirk.trainy.core.data.StationDropOffPoint
import me.cniekirk.trainy.core.data.StationDropOffSection
import me.cniekirk.trainy.core.data.StationFacilityItem
import me.cniekirk.trainy.core.data.StationHelpSection
import me.cniekirk.trainy.core.data.StationLiftInfo
import me.cniekirk.trainy.core.data.StationLiftsSection
import me.cniekirk.trainy.core.data.StationPlatformInfo
import me.cniekirk.trainy.core.data.StationPlatformsSection
import me.cniekirk.trainy.core.data.StationTicketsSection
import me.cniekirk.trainy.core.data.StationToiletsSection
import me.cniekirk.trainy.core.data.StationTransportLink
import me.cniekirk.trainy.core.data.StationWaitingSection
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun StationDetailsScreen(
    crsCode: String,
    stationName: String = "",
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StationDetailsViewModel = metroViewModel(),
) {
    val state by viewModel.collectAsState()
    LaunchedEffect(crsCode) { viewModel.load(crsCode) }

    StationDetailsContent(
        state = state,
        stationNameFallback = stationName,
        onBackClick = onBackClick,
        onRetry = { viewModel.retry(crsCode) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StationDetailsContent(
    state: StationDetailsUiState,
    stationNameFallback: String,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title =
        state.details?.name
            ?: stationNameFallback.ifBlank { stringResource(R.string.station_details_title) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription =
                                stringResource(R.string.station_details_back_button),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading ->
                    CircularProgressIndicator(
                        Modifier.align(Alignment.Center).testTag("station-details-loading")
                    )
                state.hasError ->
                    StationDetailsError(
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center),
                    )
                state.details != null -> StationDetailsBody(state.details)
            }
        }
    }
}

@Composable
private fun StationDetailsError(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.station_details_error))
        TextButton(onClick = onRetry) { Text(stringResource(R.string.station_details_retry)) }
    }
}

@Composable
private fun StationDetailsBody(details: StationDetails) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("station-details-content"),
        contentPadding = PaddingValues(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item { StationHeaderSection(details) }
        primaryDetailSections(details).forEach { section -> item { section() } }
        secondaryDetailSections(details).forEach { section -> item { section() } }
    }
}

private fun primaryDetailSections(details: StationDetails): List<@Composable () -> Unit> =
    buildList {
        if (details.alerts.isNotEmpty()) add { AlertsSection(details.alerts) }
        if (details.address != null || details.location != null) {
            add { AddressSection(details.address, details.location) }
        }
        details.accessibility?.let { section -> add { AccessibilitySection(section) } }
        if (details.facilities.isNotEmpty()) add { FacilitiesSection(details.facilities) }
        details.tickets?.let { section -> add { TicketsSection(section) } }
        details.waiting?.let { section -> add { WaitingSection(section) } }
        details.platforms?.let { section -> add { PlatformsSection(section) } }
    }

private fun secondaryDetailSections(details: StationDetails): List<@Composable () -> Unit> =
    buildList {
        if (details.transportLinks.isNotEmpty()) {
            add { TransportLinksSection(details.transportLinks) }
        }
        details.carParks?.let { section -> add { CarParksSection(section) } }
        details.cycling?.let { section -> add { CyclingSection(section) } }
        details.dropOffPickUp?.let { section -> add { DropOffSection(section) } }
        details.toilets?.let { section -> add { ToiletsSection(section) } }
        details.help?.let { section -> add { HelpSection(section) } }
        details.lifts?.let { section -> add { LiftsSection(section) } }
    }

@Composable
private fun StationHeaderSection(details: StationDetails) {
    Column(
        modifier =
            Modifier.fillMaxWidth().testTag("station-section-header").padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = details.name,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.station_details_crs, details.crsCode),
            style = MaterialTheme.typography.titleMedium,
        )
        details.operatorName?.let { operator ->
            Text(
                text = stringResource(R.string.station_details_operator, operator),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        details.staffingLevel?.let { staffing ->
            Text(
                text = stringResource(R.string.station_details_staffing, staffing),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun AlertsSection(alerts: List<StationAlert>) {
    StationSection(
        title = stringResource(R.string.station_details_section_alerts),
        testTag = "station-section-alerts",
    ) {
        alerts.forEach { alert ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                alert.title?.takeIf(String::isNotBlank)?.let { title ->
                    Text(title, style = MaterialTheme.typography.titleSmall)
                }
                HtmlText(alert.text, style = MaterialTheme.typography.bodyMedium)
                val validFrom = alert.validFrom
                if (validFrom != null) {
                    val validTo = alert.validTo
                    Text(
                        text =
                            if (validTo != null) {
                                stringResource(
                                    R.string.station_details_alert_valid_range,
                                    validFrom,
                                    validTo,
                                )
                            } else {
                                stringResource(R.string.station_details_alert_valid, validFrom)
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AddressSection(address: StationAddress?, location: StationCoordinates?) {
    StationSection(
        title = stringResource(R.string.station_details_section_address),
        testTag = "station-section-address",
    ) {
        address?.lines?.forEach { line ->
            Text(line, style = MaterialTheme.typography.bodyLarge)
        }
        address?.postcode?.let { postcode ->
            Text(postcode, style = MaterialTheme.typography.bodyLarge)
        }
        location?.let { coords ->
            Text(
                text =
                    stringResource(
                        R.string.station_details_coordinates,
                        coords.latitude,
                        coords.longitude,
                    ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AccessibilitySection(section: StationAccessibilitySection) {
    StationSection(
        title = stringResource(R.string.station_details_section_accessibility),
        testTag = "station-section-accessibility",
    ) {
        section.stepFreeCategory?.let {
            FactRow(stringResource(R.string.station_details_step_free_category), it)
        }
        section.stepFreeNotes?.let {
            FactRow(stringResource(R.string.station_details_step_free_notes), it)
        }
        section.wheelchairsAvailable?.let {
            FactRow(
                stringResource(R.string.station_details_wheelchairs),
                availabilityLabel(it),
            )
        }
        section.tactilePaving?.let {
            FactRow(stringResource(R.string.station_details_tactile_paving), it)
        }
        section.facts.forEach { FactRow(it.label, it.value) }
    }
}

@Composable
private fun FacilitiesSection(facilities: List<StationFacilityItem>) {
    StationSection(
        title = stringResource(R.string.station_details_section_facilities),
        testTag = "station-section-facilities",
    ) {
        facilities.forEach { FacilityItemRow(it) }
    }
}

@Composable
private fun TicketsSection(section: StationTicketsSection) {
    StationSection(
        title = stringResource(R.string.station_details_section_tickets),
        testTag = "station-section-tickets",
    ) {
        section.ticketOfficeAvailable?.let {
            FactRow(
                stringResource(R.string.station_details_ticket_office),
                availabilityLabel(it),
            )
        }
        section.ticketMachinesAvailable?.let {
            FactRow(
                stringResource(R.string.station_details_ticket_machines),
                availabilityLabel(it),
            )
        }
        section.londonFareZone?.let {
            FactRow(stringResource(R.string.station_details_fare_zone), it)
        }
        section.notes?.let { HtmlText(it, style = MaterialTheme.typography.bodyMedium) }
        section.facts.forEach { FactRow(it.label, it.value) }
    }
}

@Composable
private fun WaitingSection(section: StationWaitingSection) {
    StationSection(
        title = stringResource(R.string.station_details_section_waiting),
        testTag = "station-section-waiting",
    ) {
        section.shelteredWaitingAvailable?.let {
            FactRow(
                stringResource(R.string.station_details_sheltered_waiting),
                availabilityLabel(it),
            )
        }
        section.facilities.forEach { FacilityItemRow(it) }
        section.facts.forEach { FactRow(it.label, it.value) }
    }
}

@Composable
private fun PlatformsSection(section: StationPlatformsSection) {
    StationSection(
        title = stringResource(R.string.station_details_section_platforms),
        testTag = "station-section-platforms",
    ) {
        section.numberOfPlatforms?.let {
            Text(
                stringResource(R.string.station_details_platforms_count, it),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        section.entranceLevels?.let {
            FactRow(stringResource(R.string.station_details_entrance_levels), it)
        }
        section.tactileWarnings?.let {
            FactRow(stringResource(R.string.station_details_tactile_warnings), it)
        }
        section.platforms.forEach { platform -> PlatformInfoRow(platform) }
    }
}

@Composable
private fun TransportLinksSection(links: List<StationTransportLink>) {
    StationSection(
        title = stringResource(R.string.station_details_section_transport),
        testTag = "station-section-transport",
    ) {
        links.forEach { link ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(link.name, style = MaterialTheme.typography.titleSmall)
                link.available?.let {
                    Text(availabilityLabel(it), style = MaterialTheme.typography.bodyMedium)
                }
                link.location?.let { HtmlText(it, style = MaterialTheme.typography.bodyMedium) }
                link.notes?.let { HtmlText(it, style = MaterialTheme.typography.bodyMedium) }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CarParksSection(section: StationCarParksSection) {
    StationSection(
        title = stringResource(R.string.station_details_section_car_parks),
        testTag = "station-section-car-parks",
    ) {
        section.numberOfSpaces?.let {
            Text(
                stringResource(R.string.station_details_spaces, it),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        section.numberOfAccessibleSpaces?.let {
            Text(
                stringResource(R.string.station_details_accessible_spaces, it),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        section.parkingSpacesAvailable?.let {
            FactRow(
                stringResource(R.string.station_details_parking_available),
                availabilityLabel(it),
            )
        }
        section.carParks.forEach { CarParkInfoRow(it) }
    }
}

@Composable
private fun CyclingSection(section: StationCyclingSection) {
    StationSection(
        title = stringResource(R.string.station_details_section_cycling),
        testTag = "station-section-cycling",
    ) {
        section.cycleStorageAvailable?.let {
            FactRow(
                stringResource(R.string.station_details_cycle_storage),
                availabilityLabel(it),
            )
        }
        section.numberOfSpaces?.let {
            Text(
                stringResource(R.string.station_details_spaces, it),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        section.location?.let {
            FactRow(stringResource(R.string.station_details_location_label), it)
        }
        section.sheltered?.let {
            FactRow(stringResource(R.string.station_details_sheltered), yesNoLabel(it))
        }
        section.cctv?.let {
            FactRow(stringResource(R.string.station_details_cctv), yesNoLabel(it))
        }
        if (section.typesOfStorage.isNotEmpty()) {
            FactRow(
                stringResource(R.string.station_details_storage_types),
                section.typesOfStorage.joinToString(", "),
            )
        }
        section.notes?.let { HtmlText(it, style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun DropOffSection(section: StationDropOffSection) {
    StationSection(
        title = stringResource(R.string.station_details_section_drop_off),
        testTag = "station-section-drop-off",
    ) {
        section.available?.let {
            Text(availabilityLabel(it), style = MaterialTheme.typography.bodyLarge)
        }
        section.location?.let {
            FactRow(stringResource(R.string.station_details_location_label), it)
        }
        section.notes?.let { HtmlText(it, style = MaterialTheme.typography.bodyMedium) }
        section.points.forEach { point -> DropOffPointRow(point) }
    }
}

@Composable
private fun ToiletsSection(section: StationToiletsSection) {
    StationSection(
        title = stringResource(R.string.station_details_section_toilets),
        testTag = "station-section-toilets",
    ) {
        section.available?.let {
            Text(availabilityLabel(it), style = MaterialTheme.typography.bodyLarge)
        }
        section.accessibleToiletsAvailable?.let {
            FactRow(
                stringResource(R.string.station_details_accessible_toilets),
                availabilityLabel(it),
            )
        }
        section.changingPlacesAvailable?.let {
            FactRow(
                stringResource(R.string.station_details_changing_places),
                availabilityLabel(it),
            )
        }
        section.location?.let {
            FactRow(stringResource(R.string.station_details_location_label), it)
        }
        section.notes?.let { HtmlText(it, style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun HelpSection(section: StationHelpSection) {
    StationSection(
        title = stringResource(R.string.station_details_section_help),
        testTag = "station-section-help",
    ) {
        section.staffHelpAvailable?.let {
            FactRow(stringResource(R.string.station_details_staff_help), availabilityLabel(it))
        }
        section.helpPointsAvailable?.let {
            FactRow(stringResource(R.string.station_details_help_points), availabilityLabel(it))
        }
        section.helpPointsLocation?.let {
            FactRow(stringResource(R.string.station_details_location_label), it)
        }
        section.announcements?.let {
            FactRow(stringResource(R.string.station_details_announcements), it)
        }
        section.facts.forEach { FactRow(it.label, it.value) }
    }
}

@Composable
private fun LiftsSection(section: StationLiftsSection) {
    StationSection(
        title = stringResource(R.string.station_details_section_lifts),
        testTag = "station-section-lifts",
    ) {
        section.available?.let {
            Text(availabilityLabel(it), style = MaterialTheme.typography.bodyLarge)
        }
        section.statement?.let { HtmlText(it, style = MaterialTheme.typography.bodyMedium) }
        section.lifts.forEach { lift -> LiftInfoRow(lift) }
    }
}

@Composable
private fun StationSection(
    title: String,
    testTag: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag(testTag).padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        HorizontalDivider()
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }.padding(top = 8.dp),
        )
        content()
    }
}

@Composable
private fun FactRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        HtmlText(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun FacilityItemRow(item: StationFacilityItem) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(item.name, style = MaterialTheme.typography.titleSmall)
        item.available?.let {
            Text(availabilityLabel(it), style = MaterialTheme.typography.bodyMedium)
        }
        item.location?.let { HtmlText(it, style = MaterialTheme.typography.bodyMedium) }
        item.notes?.let { HtmlText(it, style = MaterialTheme.typography.bodyMedium) }
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun PlatformInfoRow(platform: StationPlatformInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(platform.name, style = MaterialTheme.typography.titleSmall)
        platform.waitingType?.let {
            Text(
                stringResource(R.string.station_details_waiting_type, it),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        platform.seatingAtIntervals?.let {
            Text(
                stringResource(R.string.station_details_seating, it),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        platform.helpPointClose?.let {
            Text(
                stringResource(R.string.station_details_help_point_close, it),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun CarParkInfoRow(carPark: StationCarParkInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(carPark.name, style = MaterialTheme.typography.titleSmall)
        carPark.numberOfSpaces?.let {
            Text(
                stringResource(R.string.station_details_spaces, it),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        carPark.numberOfAccessibleSpaces?.let {
            Text(
                stringResource(R.string.station_details_accessible_spaces, it),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        carPark.freeParking?.let {
            FactRow(stringResource(R.string.station_details_free_parking), yesNoLabel(it))
        }
        carPark.cctv?.let {
            FactRow(stringResource(R.string.station_details_cctv), yesNoLabel(it))
        }
        carPark.notes?.let { HtmlText(it, style = MaterialTheme.typography.bodyMedium) }
        carPark.charges.forEach { FactRow(it.label, it.value) }
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun DropOffPointRow(point: StationDropOffPoint) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(point.name, style = MaterialTheme.typography.titleSmall)
        point.facts.forEach { FactRow(it.label, it.value) }
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun LiftInfoRow(lift: StationLiftInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(lift.name, style = MaterialTheme.typography.titleSmall)
        lift.facts.forEach { FactRow(it.label, it.value) }
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun availabilityLabel(available: Boolean): String =
    stringResource(
        if (available) R.string.station_details_available_yes
        else R.string.station_details_available_no
    )

@Composable
private fun yesNoLabel(value: Boolean): String =
    stringResource(if (value) R.string.station_details_yes else R.string.station_details_no)

@Preview(showBackground = true)
@Composable
private fun StationDetailsContentPreview() {
    MaterialTheme {
        StationDetailsContent(
            state = StationDetailsUiState(details = previewStationDetails(), isLoading = false),
            stationNameFallback = "",
            onBackClick = {},
            onRetry = {},
        )
    }
}

private fun previewStationDetails() =
    StationDetails(
        name = "London Waterloo",
        crsCode = "WAT",
        operatorName = "South Western Railway",
        staffingLevel = "Part Time",
        address =
            StationAddress(lines = listOf("Station Approach", "London"), postcode = "SE1 8SW"),
        location = StationCoordinates(PREVIEW_LATITUDE, PREVIEW_LONGITUDE),
        alerts =
            listOf(
                StationAlert(
                    title = "Lift works",
                    text = "Platform 1 lift out of service until Friday.",
                    validFrom = "2026-07-01",
                    validTo = "2026-07-11",
                )
            ),
        accessibility =
            StationAccessibilitySection(
                stepFreeCategory = "A",
                stepFreeNotes = "Step-free to all platforms",
                wheelchairsAvailable = true,
            ),
        facilities =
            listOf(
                StationFacilityItem(name = "WiFi", available = true),
                StationFacilityItem(
                    name = "Refreshments",
                    available = false,
                    notes = "Cafe closed for refurbishment",
                ),
            ),
        tickets =
            StationTicketsSection(
                ticketOfficeAvailable = true,
                ticketMachinesAvailable = true,
                londonFareZone = "1",
            ),
        platforms =
            StationPlatformsSection(
                numberOfPlatforms = PREVIEW_PLATFORM_COUNT,
                platforms =
                    listOf(StationPlatformInfo(name = "Platform 1", waitingType = "Seated")),
            ),
        transportLinks =
            listOf(
                StationTransportLink(
                    name = "Bus",
                    available = true,
                    notes = "Outside main entrance",
                )
            ),
        toilets = StationToiletsSection(available = true, accessibleToiletsAvailable = true),
    )

private const val PREVIEW_LATITUDE = 51.5031
private const val PREVIEW_LONGITUDE = -0.1132
private const val PREVIEW_PLATFORM_COUNT = 24
