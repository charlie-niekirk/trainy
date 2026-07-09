package me.cniekirk.trainy.feature.servicelist

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import me.cniekirk.trainy.core.data.TrainService
import me.cniekirk.trainy.feature.servicedetails.ServiceDetailsRoute

@Serializable data class ServiceListRoute(val search: ServiceListSearch) : NavKey

fun EntryProviderScope<NavKey>.serviceListEntry(
    onBackClick: () -> Unit,
    onServiceClick: (ServiceDetailsRoute) -> Unit,
) {
    entry<ServiceListRoute> { route ->
        ServiceListScreen(
            route = route,
            onBackClick = onBackClick,
            onServiceClick = { service -> onServiceClick(service.toServiceDetailsRoute()) },
        )
    }
}

internal fun TrainService.toServiceDetailsRoute(): ServiceDetailsRoute = ServiceDetailsRoute(id)
