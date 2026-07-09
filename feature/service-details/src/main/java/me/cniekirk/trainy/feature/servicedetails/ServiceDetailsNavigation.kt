package me.cniekirk.trainy.feature.servicedetails

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import me.cniekirk.trainy.feature.stationdetails.StationDetailsRoute

@Serializable data class ServiceDetailsRoute(val serviceId: String) : NavKey

fun EntryProviderScope<NavKey>.serviceDetailsEntry(
    onBackClick: () -> Unit,
    onStationClick: (StationDetailsRoute) -> Unit,
) {
    entry<ServiceDetailsRoute> { route ->
        ServiceDetailsScreen(
            serviceId = route.serviceId,
            onBackClick = onBackClick,
            onStationClick = onStationClick,
        )
    }
}
