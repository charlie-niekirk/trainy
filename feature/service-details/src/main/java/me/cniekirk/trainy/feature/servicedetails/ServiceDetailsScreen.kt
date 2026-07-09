package me.cniekirk.trainy.feature.servicedetails

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.zacsweers.metrox.viewmodel.metroViewModel
import me.cniekirk.trainy.core.data.TrainServiceDetails
import me.cniekirk.trainy.core.data.TrainServiceStop
import me.cniekirk.trainy.feature.stationdetails.StationDetailsRoute
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
internal fun ServiceDetailsScreen(
    serviceId: String,
    onBackClick: () -> Unit,
    onStationClick: (StationDetailsRoute) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ServiceDetailsViewModel = metroViewModel(),
) {
    val state by viewModel.collectAsState()
    LaunchedEffect(serviceId) { viewModel.load(serviceId) }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ServiceDetailsSideEffect.NavigateToStation -> onStationClick(sideEffect.route)
        }
    }

    ServiceDetailsContent(
        state = state,
        onBackClick = onBackClick,
        onRetry = { viewModel.retry(serviceId) },
        onStopClick = viewModel::onStopSelected,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ServiceDetailsContent(
    state: ServiceDetailsUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onStopClick: (TrainServiceStop) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription =
                                stringResource(R.string.service_details_back_button),
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
                        Modifier.align(Alignment.Center).testTag("service-details-loading")
                    )
                state.hasError ->
                    ServiceDetailsError(
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center),
                    )
                state.details != null ->
                    ServiceTimeline(details = state.details, onStopClick = onStopClick)
            }
        }
    }
}

@Composable
private fun ServiceDetailsError(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.service_details_error))
        TextButton(onClick = onRetry) { Text(stringResource(R.string.service_details_retry)) }
    }
}

@Composable
private fun ServiceTimeline(
    details: TrainServiceDetails,
    onStopClick: (TrainServiceStop) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("service-details-timeline"),
        contentPadding =
            androidx.compose.foundation.layout.PaddingValues(
                start = 24.dp,
                top = 20.dp,
                end = 24.dp,
                bottom = 32.dp,
            ),
    ) {
        item {
            Text(
                text =
                    stringResource(
                        R.string.service_details_route,
                        details.origin,
                        details.destination,
                    ),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = details.operatorName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = stringResource(R.string.service_details_scheduled_time, details.time),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 2.dp, bottom = 20.dp),
            )
        }
        itemsIndexed(details.stops) { index, stop ->
            ServiceStopRow(
                stop = stop,
                index = index,
                stopCount = details.stops.size,
                isFirst = index == 0,
                isLast = index == details.stops.lastIndex,
                onClick = { onStopClick(stop) },
            )
        }
    }
}

@Composable
private fun ServiceStopRow(
    stop: TrainServiceStop,
    index: Int,
    stopCount: Int,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val description =
        stringResource(R.string.service_details_stop_description, stop.name, index + 1, stopCount)
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .height(IntrinsicSize.Min)
                .defaultMinSize(minHeight = 72.dp)
                .clickable(enabled = stop.crsCode != null, role = Role.Button, onClick = onClick)
                .testTag("service-stop-$index-${stop.name}")
                .semantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimelineMarker(isFirst = isFirst, isLast = isLast)
        Text(
            text = stop.time,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(64.dp),
        )
        Column(
            modifier = Modifier.weight(1f).padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(stop.name, style = MaterialTheme.typography.titleMedium)
            stop.platform?.let { platform ->
                Text(
                    stringResource(R.string.service_details_platform, platform),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("service-stop-platform-$index"),
                )
            }
        }
    }
}

@Composable
private fun TimelineMarker(isFirst: Boolean, isLast: Boolean) {
    val color = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier.fillMaxHeight().width(32.dp).testTag("timeline-marker"),
        contentAlignment = Alignment.Center,
    ) {
        TimelineLine(color = color, isFirst = isFirst, isLast = isLast)
        Canvas(Modifier.size(12.dp)) { drawCircle(color = color) }
    }
}

@Composable
private fun TimelineLine(color: Color, isFirst: Boolean, isLast: Boolean) {
    Canvas(Modifier.fillMaxSize()) {
        val markerCenter = Offset(size.width / 2f, size.height / 2f)
        if (!isFirst) {
            drawLine(
                color = color,
                start = Offset(markerCenter.x, 0f),
                end = markerCenter,
                strokeWidth = 2.dp.toPx(),
            )
        }
        if (!isLast) {
            drawLine(
                color = color,
                start = markerCenter,
                end = Offset(markerCenter.x, size.height),
                strokeWidth = 2.dp.toPx(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ServiceDetailsContentPreview() {
    MaterialTheme {
        ServiceDetailsContent(
            state =
                ServiceDetailsUiState(
                    details =
                        TrainServiceDetails(
                            origin = "London Waterloo",
                            destination = "Exeter St Davids",
                            operatorName = "South Western Railway",
                            time = "09:20",
                            stops =
                                listOf(
                                    TrainServiceStop("London Waterloo", "09:20", "8", "WAT"),
                                    TrainServiceStop("Salisbury", "10:42", "4", "SAL"),
                                    TrainServiceStop("Exeter St Davids", "12:15", null, "EXD"),
                                ),
                        ),
                    isLoading = false,
                ),
            onBackClick = {},
            onRetry = {},
            onStopClick = {},
        )
    }
}
