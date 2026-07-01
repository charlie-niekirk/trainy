package me.cniekirk.trainy.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.serialization.Serializable
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppNavigationStateTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun resetViewModelIds() {
        TestViewModel.nextId.set(0)
    }

    @Test
    fun navigationEntries_receiveDistinctViewModels() {
        rule.setContent {
            val startRoute = remember { TestRoute("first") }
            val navigationState =
                rememberAppNavigationState(
                    startRoute = startRoute,
                    topLevelRoutes = remember(startRoute) { setOf(startRoute) },
                )
            val navigator = remember(navigationState) { AppNavigator(navigationState) }
            val entries: (NavKey) -> NavEntry<NavKey> = entryProvider {
                entry<TestRoute> { route ->
                    val viewModel = viewModel<TestViewModel>()
                    Column {
                        Text("${route.name}:${viewModel.id}")
                        if (route == startRoute) {
                            Button(onClick = { navigator.navigate(TestRoute("second")) }) {
                                Text("Open second")
                            }
                        }
                    }
                }
            }

            NavDisplay(
                entries = navigationState.toDecoratedEntries(entries),
                onBack = navigator::goBack,
            )
        }

        rule.onNodeWithText("first:1").assertIsDisplayed()
        rule.onNodeWithText("Open second").performClick()
        rule.onNodeWithText("second:2").assertIsDisplayed()
    }
}

@Serializable private data class TestRoute(val name: String) : NavKey

class TestViewModel : ViewModel() {
    val id: Int = nextId.incrementAndGet()

    companion object {
        val nextId = AtomicInteger()
    }
}
