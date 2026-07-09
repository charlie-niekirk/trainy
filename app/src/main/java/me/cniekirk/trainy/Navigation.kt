package me.cniekirk.trainy

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import me.cniekirk.trainy.feature.favourites.favouritesEntry
import me.cniekirk.trainy.feature.search.searchEntry
import me.cniekirk.trainy.feature.servicedetails.serviceDetailsEntry
import me.cniekirk.trainy.feature.servicelist.serviceListEntry
import me.cniekirk.trainy.feature.settings.settingsEntry
import me.cniekirk.trainy.feature.stationdetails.stationDetailsEntry
import me.cniekirk.trainy.feature.stationsearch.stationSearchEntry
import me.cniekirk.trainy.navigation.AppNavigator
import me.cniekirk.trainy.navigation.TopLevelDestinations
import me.cniekirk.trainy.navigation.rememberAppNavigationState

@Composable
fun MainNavigation(modifier: Modifier = Modifier) {
    val navigationState =
        rememberAppNavigationState(
            startRoute = TopLevelDestinations.first().route,
            topLevelRoutes = TopLevelDestinations.map { it.route }.toSet(),
        )
    val navigator = remember(navigationState) { AppNavigator(navigationState) }
    val entryProvider = entryProvider {
        searchEntry(
            onSearchSubmitted = navigator::navigate,
            onStationSearch = navigator::navigate,
        )
        favouritesEntry(onServiceClick = navigator::navigate)
        settingsEntry()
        serviceDetailsEntry(
            onBackClick = navigator::goBack,
            onStationClick = navigator::navigate,
        )
        stationDetailsEntry(onBackClick = navigator::goBack)
        serviceListEntry(
            onBackClick = navigator::goBack,
            onServiceClick = navigator::navigate,
        )
        stationSearchEntry(onBackClick = navigator::goBack)
    }

    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteItems = {
            TopLevelDestinations.forEach { destination ->
                item(
                    selected = destination.route == navigationState.topLevelRoute,
                    onClick = { navigator.navigate(destination.route) },
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                        )
                    },
                    label = { Text(destination.label) },
                )
            }
        },
    ) {
        NavDisplay(
            entries = navigationState.toDecoratedEntries(entryProvider),
            onBack = navigator::goBack,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
