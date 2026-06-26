package me.cniekirk.trainy.feature.favourites

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import me.cniekirk.trainy.feature.servicedetails.ServiceDetailsRoute

@Serializable data object FavouritesRoute : NavKey

fun EntryProviderScope<NavKey>.favouritesEntry(onServiceClick: (ServiceDetailsRoute) -> Unit) {
    entry<FavouritesRoute> {
        TrackedScreen(
            onServiceClick = { service ->
                onServiceClick(service.toServiceDetailsRoute())
            }
        )
    }
}

internal fun TrackedServiceUiModel.toServiceDetailsRoute(): ServiceDetailsRoute =
    ServiceDetailsRoute(serviceId)
