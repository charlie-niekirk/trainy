package me.cniekirk.trainy.feature.servicedetails

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class ServiceDetailsScreenTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun serviceId_isDisplayed() {
        rule.setContent {
            MaterialTheme {
                ServiceDetailsScreen(
                    serviceId = "gb-nr:L79342:2026-06-19",
                    onBackClick = {},
                )
            }
        }

        rule.onNodeWithText("Service details").assertIsDisplayed()
        rule.onNodeWithText("gb-nr:L79342:2026-06-19").assertIsDisplayed()
    }
}
