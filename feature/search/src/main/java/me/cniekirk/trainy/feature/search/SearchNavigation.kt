package me.cniekirk.trainy.feature.search

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SearchRoute : NavKey

@Serializable
data class DepartureBoardRoute(
    val search: DepartureBoardSearch,
) : NavKey

fun EntryProviderScope<NavKey>.searchEntry(onSearchSubmitted: (DepartureBoardRoute) -> Unit) {
    entry<SearchRoute> {
        SearchScreen(onSearchSubmitted = onSearchSubmitted)
    }
}

fun EntryProviderScope<NavKey>.departureBoardEntry(onBackClick: () -> Unit) {
    entry<DepartureBoardRoute> { route ->
        DepartureBoardPlaceholderScreen(
            route = route,
            onBackClick = onBackClick,
        )
    }
}
