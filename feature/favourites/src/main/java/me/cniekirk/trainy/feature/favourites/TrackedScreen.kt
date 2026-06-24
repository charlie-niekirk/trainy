package me.cniekirk.trainy.feature.favourites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun TrackedScreen(
    onServiceClick: (TrackedServiceUiModel) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrackedViewModel = metroViewModel(),
) {
    val state by viewModel.collectAsState()

    TrackedContent(
        state = state,
        onServiceClick = onServiceClick,
        modifier = modifier,
    )
}
