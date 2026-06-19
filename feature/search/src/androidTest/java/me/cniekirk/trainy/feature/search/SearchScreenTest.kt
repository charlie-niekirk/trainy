package me.cniekirk.trainy.feature.search

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import me.cniekirk.trainy.feature.stationsearch.StationField
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {
    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun departingMode_showsDepartingLabels() {
        composeTestRule.setContent {
            MaterialTheme {
                SearchScreenContent(
                    state =
                        SearchUiState(
                            date = "2026-06-13",
                            time = "14:30",
                        ),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Departing from").assertIsDisplayed()
        composeTestRule.onNodeWithText("Going to (optional)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Date").assertIsDisplayed()
        composeTestRule.onNodeWithText("Time").assertIsDisplayed()
    }

    @Test
    fun arrivingMode_showsArrivingLabels() {
        composeTestRule.setContent {
            MaterialTheme {
                SearchScreenContent(
                    state =
                        SearchUiState(
                            mode = SearchMode.Arriving,
                            date = "2026-06-13",
                            time = "14:30",
                        ),
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Arriving at").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coming from (optional)").assertIsDisplayed()
    }

    @Test
    fun dateTimeControls_emitActionsAfterPickerConfirmation() {
        val actions = mutableListOf<SearchAction>()

        composeTestRule.setContent {
            MaterialTheme {
                SearchScreenContent(
                    state =
                        SearchUiState(
                            date = "2026-06-13",
                            time = "14:30",
                        ),
                    onAction = actions::add,
                )
            }
        }

        composeTestRule.onNodeWithTag("search-date").performClick()
        composeTestRule.onNodeWithText("OK").performClick()
        composeTestRule.onNodeWithTag("search-time").performClick()
        composeTestRule.onNodeWithText("OK").performClick()

        assert(actions.contains(SearchAction.DateChanged("2026-06-13")))
        assert(actions.contains(SearchAction.TimeChanged("14:30")))
    }

    @Test
    fun formControls_emitActions() {
        val actions = mutableListOf<SearchAction>()

        composeTestRule.setContent {
            MaterialTheme {
                SearchScreenContent(
                    state =
                        SearchUiState(
                            date = "2026-06-13",
                            time = "14:30",
                        ),
                    onAction = actions::add,
                )
            }
        }

        composeTestRule.onNodeWithText("Arriving").performClick()
        composeTestRule.onNodeWithTag("target-station").performClick()
        composeTestRule.onNodeWithTag("filter-station").performClick()
        composeTestRule.onNodeWithTag("search-button").performClick()

        assert(actions.contains(SearchAction.ModeSelected(SearchMode.Arriving)))
        assert(actions.contains(SearchAction.SelectStation(StationField.Target)))
        assert(actions.contains(SearchAction.SelectStation(StationField.Filter)))
        assert(actions.contains(SearchAction.SearchClicked))
    }
}
