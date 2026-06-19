package me.cniekirk.trainy.feature.stationsearch

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import me.cniekirk.trainy.core.data.Station
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StationSearchScreenTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun stationsShowNameAndCrs_andControlsEmitActions() {
        val station = Station("London Kings Cross", "KGX")
        val actions = mutableListOf<StationSearchAction>()
        rule.setContent {
            MaterialTheme {
                StationSearchContent(
                    state = StationSearchUiState(stations = listOf(station), isLoading = false),
                    onBackClick = {},
                    onAction = actions::add,
                )
            }
        }

        rule.onNodeWithText("London Kings Cross").assertIsDisplayed()
        rule.onNodeWithText("KGX").assertIsDisplayed()
        rule.onNodeWithTag("station-query").performTextInput("kgx")
        rule.onNodeWithTag("station-KGX").performClick()

        assertTrue(actions.contains(StationSearchAction.QueryChanged("kgx")))
        assertTrue(actions.contains(StationSearchAction.StationSelected(station)))
    }
}
