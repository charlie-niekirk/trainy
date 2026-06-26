package me.cniekirk.trainy.feature.favourites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
private fun TrackedScreenPreview() {
    MaterialTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
            Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(padding)) {
                Text(
                    text = "Tracked",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                )
                LazyColumn(Modifier.fillMaxSize()) {
                    item {
                        TrackedPreviewRow(
                            time = "09:20",
                            destination = "Exeter St Davids",
                            operatorName = "South Western Railway",
                            platform = "Platform 8",
                            platformConfirmed = false,
                        )
                        HorizontalDivider()
                    }
                    item {
                        TrackedPreviewRow(
                            time = "09:50",
                            destination = "Yeovil Junction",
                            operatorName = "South Western Railway",
                            platform = "Platform 12",
                            platformConfirmed = true,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackedPreviewRow(
    time: String,
    destination: String,
    operatorName: String,
    platform: String,
    platformConfirmed: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(time, style = MaterialTheme.typography.titleMedium)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(destination, style = MaterialTheme.typography.titleMedium)
            Text(operatorName, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = platform,
            fontWeight = if (platformConfirmed) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
