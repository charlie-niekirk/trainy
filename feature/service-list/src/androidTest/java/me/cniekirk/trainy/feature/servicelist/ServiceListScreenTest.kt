package me.cniekirk.trainy.feature.servicelist

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import me.cniekirk.trainy.core.data.TrainService
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ServiceListScreenTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun departingServices_showHeadingDetailsAndPlatformCertainty() {
        val services =
            listOf(
                TrainService(
                    id = "estimated",
                    time = "09:20",
                    destination = "Exeter St Davids",
                    platform = "8",
                    isPlatformConfirmed = false,
                    operatorName = "South Western Railway",
                ),
                TrainService(
                    id = "confirmed",
                    time = "09:50",
                    destination = "Salisbury",
                    platform = "12",
                    isPlatformConfirmed = true,
                    operatorName = "South Western Railway",
                ),
            )
        rule.setContent {
            MaterialTheme {
                ServiceListContent(
                    search = departingSearch(),
                    state = ServiceListUiState(services = services, isLoading = false),
                    onBackClick = {},
                    onRetry = {},
                )
            }
        }

        rule.onAllNodesWithText("Departing London Waterloo (via Salisbury)").assertCountEquals(1)
        rule.onAllNodesWithText("Departure board").assertCountEquals(0)
        rule.onNodeWithTag("service-list").assertIsDisplayed()
        rule.onNodeWithText("09:20").assertIsDisplayed()
        rule.onNodeWithText("Exeter St Davids").assertIsDisplayed()
        rule.onAllNodesWithText("South Western Railway").assertCountEquals(2)
        rule
            .onNodeWithText("Platform 8")
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "Estimated platform",
                )
            )
        rule
            .onNodeWithText("Platform 12")
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "Confirmed platform",
                )
            )
    }

    @Test
    fun arrivingSearch_showsFromHeadingAndRetryAction() {
        var retried = false
        rule.setContent {
            MaterialTheme {
                ServiceListContent(
                    search = departingSearch().copy(mode = ServiceListMode.Arriving),
                    state = ServiceListUiState(isLoading = false, hasError = true),
                    onBackClick = {},
                    onRetry = { retried = true },
                )
            }
        }

        rule.onAllNodesWithText("Arriving at London Waterloo (from Salisbury)").assertCountEquals(1)
        rule.onNodeWithText("Try again").performClick()
        assertTrue(retried)
    }

    private fun departingSearch() =
        ServiceListSearch(
            mode = ServiceListMode.Departing,
            targetStation = "WAT",
            targetStationName = "London Waterloo",
            filterStation = "SAL",
            filterStationName = "Salisbury",
            dateTimeMillis = 1_781_856_000_000L,
        )
}
