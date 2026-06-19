package me.cniekirk.trainy.feature.stationsearch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.result.LocalResultEventBus
import dev.zacsweers.metrox.viewmodel.metroViewModel
import me.cniekirk.trainy.core.data.Station
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
internal fun StationSearchScreen(
    field: StationField,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StationSearchViewModel = metroViewModel(),
) {
    val state by viewModel.collectAsState()
    val resultBus = LocalResultEventBus.current
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is StationSearchSideEffect.ReturnSelection -> {
                resultBus.sendResult(effect.result)
                onBackClick()
            }
        }
    }
    StationSearchContent(
        state = state,
        onBackClick = onBackClick,
        onAction = { action ->
            when (action) {
                is StationSearchAction.QueryChanged -> viewModel.onQueryChanged(action.value)
                is StationSearchAction.StationSelected ->
                    viewModel.onStationSelected(field, action.station)
                StationSearchAction.Retry -> viewModel.retry()
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StationSearchContent(
    state: StationSearchUiState,
    onBackClick: () -> Unit,
    onAction: (StationSearchAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.station_search_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.station_search_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onAction(StationSearchAction.QueryChanged(it)) },
                modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("station-query"),
                label = { Text(stringResource(R.string.station_search_hint)) },
                singleLine = true,
            )
            when {
                state.isLoading ->
                    CircularProgressIndicator(
                        modifier =
                            Modifier.align(Alignment.CenterHorizontally).testTag("station-loading")
                    )
                state.hasError ->
                    TextButton(
                        onClick = { onAction(StationSearchAction.Retry) },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text(stringResource(R.string.station_search_retry))
                    }
                state.stations.isEmpty() ->
                    Text(
                        stringResource(R.string.station_search_empty),
                        modifier = Modifier.padding(24.dp).align(Alignment.CenterHorizontally),
                    )
                else -> StationList(state.stations, onAction)
            }
        }
    }
}

@Composable
private fun StationList(stations: List<Station>, onAction: (StationSearchAction) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().testTag("station-list")) {
        items(stations, key = Station::crsCode) { station ->
            ListItem(
                headlineContent = { Text(station.name) },
                supportingContent = { Text(station.crsCode) },
                modifier =
                    Modifier.fillMaxWidth()
                        .clickable { onAction(StationSearchAction.StationSelected(station)) }
                        .testTag("station-${station.crsCode}"),
            )
            HorizontalDivider()
        }
    }
}
