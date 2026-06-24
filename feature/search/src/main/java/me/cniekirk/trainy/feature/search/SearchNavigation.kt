package me.cniekirk.trainy.feature.search

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import me.cniekirk.trainy.feature.servicelist.ServiceListRoute
import me.cniekirk.trainy.feature.stationsearch.StationSearchRoute

@Serializable data object SearchRoute : NavKey

fun EntryProviderScope<NavKey>.searchEntry(
    onSearchSubmitted: (ServiceListRoute) -> Unit,
    onStationSearch: (StationSearchRoute) -> Unit,
) {
    entry<SearchRoute> {
        SearchScreen(
            onSearchSubmitted = onSearchSubmitted,
            onStationSearch = onStationSearch,
        )
    }
}
