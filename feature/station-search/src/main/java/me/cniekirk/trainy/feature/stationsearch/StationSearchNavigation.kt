package me.cniekirk.trainy.feature.stationsearch

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.stationSearchEntry(onBackClick: () -> Unit) {
    entry<StationSearchRoute> { route ->
        StationSearchScreen(field = route.field, onBackClick = onBackClick)
    }
}
