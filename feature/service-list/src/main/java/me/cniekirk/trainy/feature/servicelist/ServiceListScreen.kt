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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.zacsweers.metrox.viewmodel.metroViewModel
import me.cniekirk.trainy.core.data.TrainService
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun ServiceListScreen(
    route: ServiceListRoute,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ServiceListViewModel = metroViewModel(),
) {
    val state by viewModel.collectAsState()
    LaunchedEffect(route.search) { viewModel.load(route.search) }

    ServiceListContent(
        search = route.search,
        state = state,
        onBackClick = onBackClick,
        onRetry = { viewModel.retry(route.search) },
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
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
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
                else -> ServiceList(state.services)
            }
        }
    }
}

@Composable
private fun ServiceList(services: List<TrainService>) {
    LazyColumn(Modifier.fillMaxSize().testTag("service-list")) {
        items(services, key = TrainService::id) { service ->
            ServiceListItem(service)
            HorizontalDivider()
        }
    }
}

@Composable
private fun ServiceListItem(service: TrainService) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
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
