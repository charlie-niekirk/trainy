package me.cniekirk.trainy.feature.favourites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.zacsweers.metrox.viewmodel.metroViewModel
import me.cniekirk.trainy.core.data.TrackedTrainService
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun TrackedScreen(
    onServiceClick: (TrackedTrainService) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrackedViewModel = metroViewModel(),
) {
    val state by viewModel.collectAsState()

    TrackedContent(
        state = state,
        onServiceClick = onServiceClick,
        modifier = modifier,
    )
}

@Composable
internal fun TrackedContent(
    state: TrackedUiState,
    onServiceClick: (TrackedTrainService) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(padding)) {
            Text(
                text = stringResource(R.string.tracked_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            )
            when {
                state.isLoading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.testTag("tracked-loading"))
                    }

                state.services.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.tracked_empty))
                    }

                else -> TrackedServiceList(state.services, onServiceClick)
            }
        }
    }
}

@Composable
private fun TrackedServiceList(
    services: List<TrackedTrainService>,
    onServiceClick: (TrackedTrainService) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize().testTag("tracked-service-list")) {
        items(services, key = TrackedTrainService::serviceId) { service ->
            TrackedServiceItem(service, onServiceClick)
            HorizontalDivider()
        }
    }
}

@Composable
private fun TrackedServiceItem(
    service: TrackedTrainService,
    onServiceClick: (TrackedTrainService) -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable(role = Role.Button) { onServiceClick(service) }
                .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(service.time, style = MaterialTheme.typography.titleMedium)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(service.destination, style = MaterialTheme.typography.titleMedium)
            Text(service.operatorName, style = MaterialTheme.typography.bodyMedium)
        }
        val platformDescription =
            stringResource(
                if (service.isPlatformConfirmed) R.string.tracked_platform_confirmed
                else R.string.tracked_platform_estimated
            )
        Text(
            text =
                service.platform?.let { stringResource(R.string.tracked_platform, it) }
                    ?: stringResource(R.string.tracked_platform_unavailable),
            fontWeight = if (service.isPlatformConfirmed) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.semantics { stateDescription = platformDescription },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrackedScreenPreview() {
    MaterialTheme {
        TrackedContent(
            state =
                TrackedUiState(
                    services =
                        listOf(
                            TrackedTrainService(
                                serviceId = "gb-nr:L79342:2026-06-19",
                                time = "09:20",
                                destination = "Exeter St Davids",
                                platform = "8",
                                isPlatformConfirmed = false,
                                operatorName = "South Western Railway",
                                trackedAtEpochMillis = 1_800_000_000_000L,
                            ),
                            TrackedTrainService(
                                serviceId = "gb-nr:L80061:2026-06-19",
                                time = "09:50",
                                destination = "Yeovil Junction",
                                platform = "12",
                                isPlatformConfirmed = true,
                                operatorName = "South Western Railway",
                                trackedAtEpochMillis = 1_800_000_001_000L,
                            ),
                        ),
                    isLoading = false,
                ),
            onServiceClick = {},
        )
    }
}
