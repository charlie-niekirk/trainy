package me.cniekirk.trainy.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import me.cniekirk.trainy.feature.favourites.FavouritesRoute
import me.cniekirk.trainy.feature.search.SearchRoute
import me.cniekirk.trainy.feature.settings.SettingsRoute

data class TopLevelDestination(
    val route: NavKey,
    val label: String,
    val icon: ImageVector,
)

val TopLevelDestinations =
    listOf(
        TopLevelDestination(
            route = SearchRoute,
            label = "Search",
            icon = Icons.Filled.Search,
        ),
        TopLevelDestination(
            route = FavouritesRoute,
            label = "Favourites",
            icon = Icons.Filled.Favorite,
        ),
        TopLevelDestination(
            route = SettingsRoute,
            label = "Settings",
            icon = Icons.Filled.Settings,
        ),
    )
