package me.cniekirk.trainy.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SearchRoute : NavKey

fun EntryProviderScope<NavKey>.searchEntry(onStationSearchClick: () -> Unit) {
    entry<SearchRoute> {
        SearchScreen(onStationSearchClick = onStationSearchClick)
    }
}

@Composable
internal fun SearchScreen(
    onStationSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FeatureStub(
        title = "Search",
        actionLabel = "Station search",
        onActionClick = onStationSearchClick,
        modifier = modifier,
    )
}

@Composable
private fun FeatureStub(
    title: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onActionClick, modifier = Modifier.padding(top = 16.dp)) {
            Text(actionLabel)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    MaterialTheme {
        SearchScreen(onStationSearchClick = {})
    }
}
