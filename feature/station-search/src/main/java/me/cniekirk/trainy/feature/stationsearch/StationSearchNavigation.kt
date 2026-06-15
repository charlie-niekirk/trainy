package me.cniekirk.trainy.feature.stationsearch

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

@Serializable data object StationSearchRoute : NavKey

fun EntryProviderScope<NavKey>.stationSearchEntry(onBackClick: () -> Unit) {
    entry<StationSearchRoute> {
        StationSearchScreen(onBackClick = onBackClick)
    }
}

@Composable
internal fun StationSearchScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Station search", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onBackClick, modifier = Modifier.padding(top = 16.dp)) {
            Text("Back")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StationSearchScreenPreview() {
    MaterialTheme {
        StationSearchScreen(onBackClick = {})
    }
}
