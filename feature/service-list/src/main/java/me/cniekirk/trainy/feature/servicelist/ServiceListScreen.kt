package me.cniekirk.trainy.feature.servicelist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.zacsweers.metrox.viewmodel.metroViewModel
import me.cniekirk.trainy.core.data.TrainService
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
internal fun ServiceListScreen(
    route: ServiceListRoute,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ServiceListViewModel = metroViewModel(),
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val trackingErrorMessage = stringResource(R.string.service_list_tracking_error)
    LaunchedEffect(route.search) { viewModel.load(route.search) }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            ServiceListSideEffect.ShowTrackingError ->
                snackbarHostState.showSnackbar(trackingErrorMessage)
        }
    }

    ServiceListContent(
        search = route.search,
        state = state,
        onBackClick = onBackClick,
        onRetry = { viewModel.retry(route.search) },
        onTrackClick = viewModel::onTrackingClick,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ServiceListContent(
    search: ServiceListSearch,
    state: ServiceListUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onTrackClick: (TrainService) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = snackbarHost,
        topBar = {
            TopAppBar(
                title = { Text(search.heading()) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.service_list_back_button),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.testTag("services-loading"))
                    }
                state.hasError ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        TextButton(onClick = onRetry) {
                            Text(stringResource(R.string.service_list_retry))
                        }
                    }
                state.services.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.service_list_empty))
                    }
                else -> ServiceList(state.services, state.trackedServiceIds, onTrackClick)
            }
        }
    }
}

@Composable
private fun ServiceList(
    services: List<TrainService>,
    trackedServiceIds: Set<String>,
    onTrackClick: (TrainService) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize().testTag("service-list")) {
        items(services, key = TrainService::id) { service ->
            ServiceListItem(
                service = service,
                isTracked = service.id in trackedServiceIds,
                onTrackClick = onTrackClick,
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun ServiceListItem(
    service: TrainService,
    isTracked: Boolean,
    onTrackClick: (TrainService) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
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
                if (service.isPlatformConfirmed) R.string.service_list_platform_confirmed
                else R.string.service_list_platform_estimated
            )
        Text(
            text =
                service.platform?.let { stringResource(R.string.service_list_platform, it) }
                    ?: stringResource(R.string.service_list_platform_unavailable),
            fontWeight = if (service.isPlatformConfirmed) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.semantics { stateDescription = platformDescription },
        )
        val trackingDescription =
            stringResource(
                if (isTracked) R.string.service_list_untrack_service
                else R.string.service_list_track_service,
                service.destination,
            )
        IconToggleButton(
            checked = isTracked,
            onCheckedChange = { onTrackClick(service) },
            modifier = Modifier.testTag("track-service-${service.id}"),
        ) {
            Icon(
                imageVector = if (isTracked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = trackingDescription,
            )
        }
    }
}

private fun ServiceListSearch.heading(): String {
    val target = targetStationName.ifBlank { targetStation }
    val filter = filterStationName.ifBlank { filterStation.orEmpty() }
    return when (mode) {
        ServiceListMode.Departing ->
            if (filter.isBlank()) "Departing $target" else "Departing $target (via $filter)"
        ServiceListMode.Arriving ->
            if (filter.isBlank()) "Arriving at $target" else "Arriving at $target (from $filter)"
    }
}
