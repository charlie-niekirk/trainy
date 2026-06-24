package me.cniekirk.trainy.feature.servicelist

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data class ServiceListRoute(val search: ServiceListSearch) : NavKey

fun EntryProviderScope<NavKey>.serviceListEntry(onBackClick: () -> Unit) {
    entry<ServiceListRoute> { route ->
        ServiceListScreen(
            route = route,
            onBackClick = onBackClick,
        )
    }
}
