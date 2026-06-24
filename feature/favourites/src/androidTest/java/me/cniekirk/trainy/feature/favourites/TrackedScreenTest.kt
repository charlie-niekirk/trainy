package me.cniekirk.trainy.feature.favourites

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
import me.cniekirk.trainy.core.data.TrackedTrainService
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TrackedScreenTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyState_isDisplayed() {
        rule.setContent {
            MaterialTheme {
                TrackedContent(
                    state = TrackedUiState(isLoading = false),
                    onServiceClick = {},
                )
            }
        }

        rule.onNodeWithText("Tracked").assertIsDisplayed()
        rule.onNodeWithText("No tracked services yet").assertIsDisplayed()
    }

    @Test
    fun trackedServices_areDisplayedWithPlatformCertainty() {
        rule.setContent {
            MaterialTheme {
                TrackedContent(
                    state =
                        TrackedUiState(
                            services =
                                listOf(
                                    trackedService(
                                        serviceId = "estimated",
                                        platform = "8",
                                        isPlatformConfirmed = false,
                                    ),
                                    trackedService(
                                        serviceId = "confirmed",
                                        platform = "12",
                                        isPlatformConfirmed = true,
                                    ),
                                ),
                            isLoading = false,
                        ),
                    onServiceClick = {},
                )
            }
        }

        rule.onNodeWithTag("tracked-service-list").assertIsDisplayed()
        rule.onAllNodesWithText("09:20").assertCountEquals(2)
        rule.onAllNodesWithText("Exeter St Davids").assertCountEquals(2)
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
    fun trackedServiceClick_callsCallback() {
        val service = trackedService(serviceId = "gb-nr:L79342:2026-06-19")
        var clickedService: TrackedTrainService? = null
        rule.setContent {
            MaterialTheme {
                TrackedContent(
                    state = TrackedUiState(services = listOf(service), isLoading = false),
                    onServiceClick = { clickedService = it },
                )
            }
        }

        rule.onNodeWithText("Exeter St Davids").performClick()

        assertEquals(service, clickedService)
    }

    private fun trackedService(
        serviceId: String,
        platform: String? = "8",
        isPlatformConfirmed: Boolean = false,
    ) =
        TrackedTrainService(
            serviceId = serviceId,
            time = "09:20",
            destination = "Exeter St Davids",
            platform = platform,
            isPlatformConfirmed = isPlatformConfirmed,
            operatorName = "South Western Railway",
            trackedAtEpochMillis = 1_800_000_000_000L,
        )
}
