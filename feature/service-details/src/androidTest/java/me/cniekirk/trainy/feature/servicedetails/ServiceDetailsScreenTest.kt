package me.cniekirk.trainy.feature.servicedetails

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import me.cniekirk.trainy.core.data.TrainServiceDetails
import me.cniekirk.trainy.core.data.TrainServiceStop
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ServiceDetailsScreenTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loadingState_showsProgressAndBackAction() {
        var wentBack = false
        setContent(ServiceDetailsUiState(), onBackClick = { wentBack = true })

        rule.onNodeWithTag("service-details-loading").assertIsDisplayed()
        rule.onNodeWithContentDescription("Back").performClick()

        assertTrue(wentBack)
    }

    @Test
    fun errorState_showsRetryAction() {
        var retried = false
        setContent(
            ServiceDetailsUiState(isLoading = false, hasError = true),
            onRetry = { retried = true },
        )

        rule.onNodeWithText("Unable to load service details").assertIsDisplayed()
        rule.onNodeWithText("Try again").performClick()

        assertTrue(retried)
    }

    @Test
    fun content_showsHeaderOrderedStopsAndOptionalPlatforms() {
        setContent(ServiceDetailsUiState(details = details(), isLoading = false))

        rule.onNodeWithText("London Waterloo → Exeter St Davids").assertIsDisplayed()
        rule.onNodeWithText("South Western Railway").assertIsDisplayed()
        rule.onNodeWithText("Scheduled 09:20").assertIsDisplayed()
        rule.onNodeWithTag("service-stop-0-London Waterloo").assertIsDisplayed()
        rule.onNodeWithTag("service-stop-1-Salisbury").assertIsDisplayed()
        rule.onNodeWithTag("service-stop-2-Exeter St Davids").assertIsDisplayed()
        rule.onNodeWithText("Platform 8").assertIsDisplayed()
        rule.onNodeWithText("Platform 4").assertIsDisplayed()
        rule.onAllNodesWithTag("service-stop-platform-0").assertCountEquals(1)
        rule.onAllNodesWithTag("service-stop-platform-1").assertCountEquals(1)
        rule.onAllNodesWithTag("service-stop-platform-2").assertCountEquals(0)
    }

    @Test
    fun timeline_hasOneAccessibleMarkerForEachStop() {
        setContent(ServiceDetailsUiState(details = details(), isLoading = false))

        rule.onAllNodesWithTag("timeline-marker").assertCountEquals(3)
        rule.onNodeWithContentDescription("London Waterloo, stop 1 of 3").assertIsDisplayed()
        rule.onNodeWithContentDescription("Exeter St Davids, stop 3 of 3").assertIsDisplayed()
    }

    @Test
    fun stopWithCrs_invokesOnStopClick() {
        var selected: TrainServiceStop? = null
        setContent(
            ServiceDetailsUiState(details = details(), isLoading = false),
            onStopClick = { selected = it },
        )

        rule.onNodeWithTag("service-stop-1-Salisbury").performClick()

        assertEquals(TrainServiceStop("Salisbury", "10:42", "4", "SAL"), selected)
    }

    @Test
    fun stopWithoutCrs_doesNotInvokeOnStopClick() {
        var selected: TrainServiceStop? = null
        setContent(
            ServiceDetailsUiState(
                details =
                    TrainServiceDetails(
                        origin = "A",
                        destination = "B",
                        operatorName = "Operator",
                        time = "09:00",
                        stops = listOf(TrainServiceStop("Unknown", "09:00", null, null)),
                    ),
                isLoading = false,
            ),
            onStopClick = { selected = it },
        )

        rule.onNodeWithTag("service-stop-0-Unknown").performClick()

        assertNull(selected)
    }

    private fun setContent(
        state: ServiceDetailsUiState,
        onBackClick: () -> Unit = {},
        onRetry: () -> Unit = {},
        onStopClick: (TrainServiceStop) -> Unit = {},
    ) {
        rule.setContent {
            MaterialTheme {
                ServiceDetailsContent(
                    state = state,
                    onBackClick = onBackClick,
                    onRetry = onRetry,
                    onStopClick = onStopClick,
                )
            }
        }
    }

    private fun details() =
        TrainServiceDetails(
            origin = "London Waterloo",
            destination = "Exeter St Davids",
            operatorName = "South Western Railway",
            time = "09:20",
            stops =
                listOf(
                    TrainServiceStop("London Waterloo", "09:20", "8", "WAT"),
                    TrainServiceStop("Salisbury", "10:42", "4", "SAL"),
                    TrainServiceStop("Exeter St Davids", "12:15", null, "EXD"),
                ),
        )
}
