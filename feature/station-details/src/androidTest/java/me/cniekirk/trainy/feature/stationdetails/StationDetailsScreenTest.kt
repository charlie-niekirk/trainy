package me.cniekirk.trainy.feature.stationdetails

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import me.cniekirk.trainy.core.data.StationAlert
import me.cniekirk.trainy.core.data.StationDetails
import me.cniekirk.trainy.core.data.StationFacilityItem
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StationDetailsScreenTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loadingState_showsProgressAndBackAction() {
        var wentBack = false
        setContent(StationDetailsUiState(), onBackClick = { wentBack = true })

        rule.onNodeWithTag("station-details-loading").assertIsDisplayed()
        rule.onNodeWithContentDescription("Back").performClick()

        assertTrue(wentBack)
    }

    @Test
    fun errorState_showsRetryAction() {
        var retried = false
        setContent(
            StationDetailsUiState(isLoading = false, hasError = true),
            onRetry = { retried = true },
        )

        rule.onNodeWithText("Unable to load station details").assertIsDisplayed()
        rule.onNodeWithText("Try again").performClick()

        assertTrue(retried)
    }

    @Test
    fun content_showsNameCrsAndSections() {
        setContent(StationDetailsUiState(details = details(), isLoading = false))

        rule.onNodeWithTag("station-details-content").assertIsDisplayed()
        // Name appears in the top app bar and the in-content header.
        rule.onAllNodesWithText("London Waterloo").assertCountEquals(2)
        rule.onNodeWithText("CRS WAT").assertIsDisplayed()
        rule.onNodeWithText("Operator: South Western Railway").assertIsDisplayed()
        rule.onNodeWithTag("station-section-alerts").assertIsDisplayed()
        rule.onNodeWithText("Lift works").assertIsDisplayed()
        rule.onNodeWithTag("station-section-facilities").assertIsDisplayed()
        rule.onNodeWithText("WiFi").assertIsDisplayed()
    }

    private fun setContent(
        state: StationDetailsUiState,
        onBackClick: () -> Unit = {},
        onRetry: () -> Unit = {},
        stationNameFallback: String = "",
    ) {
        rule.setContent {
            MaterialTheme {
                StationDetailsContent(
                    state = state,
                    stationNameFallback = stationNameFallback,
                    onBackClick = onBackClick,
                    onRetry = onRetry,
                )
            }
        }
    }

    private fun details() =
        StationDetails(
            name = "London Waterloo",
            crsCode = "WAT",
            operatorName = "South Western Railway",
            staffingLevel = "Part Time",
            alerts =
                listOf(
                    StationAlert(
                        title = "Lift works",
                        text = "Platform 1 lift out of service until Friday.",
                        validFrom = "2026-07-01",
                        validTo = "2026-07-11",
                    )
                ),
            facilities = listOf(StationFacilityItem(name = "WiFi", available = true)),
        )
}
