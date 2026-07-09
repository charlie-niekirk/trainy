package me.cniekirk.trainy.feature.stationdetails

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data class StationDetailsRoute(val crsCode: String, val name: String = "") : NavKey

fun EntryProviderScope<NavKey>.stationDetailsEntry(onBackClick: () -> Unit) {
    entry<StationDetailsRoute> { route ->
        StationDetailsScreen(
            crsCode = route.crsCode,
            stationName = route.name,
            onBackClick = onBackClick,
        )
    }
}
