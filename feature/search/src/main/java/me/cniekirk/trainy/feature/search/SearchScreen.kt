package me.cniekirk.trainy.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.result.ResultEffect
import dev.zacsweers.metrox.viewmodel.metroViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import me.cniekirk.trainy.feature.stationsearch.StationField
import me.cniekirk.trainy.feature.stationsearch.StationSearchRoute
import me.cniekirk.trainy.feature.stationsearch.StationSelectionResult
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
internal fun SearchScreen(
    onSearchSubmitted: (DepartureBoardRoute) -> Unit,
    onStationSearch: (StationSearchRoute) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = metroViewModel(),
) {
    val state by viewModel.collectAsState()

    ResultEffect<StationSelectionResult> { viewModel.onStationSelected(it) }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SearchSideEffect.NavigateToDepartureBoard -> onSearchSubmitted(sideEffect.route)
        }
    }

    SearchScreenContent(
        state = state,
        onAction = { action ->
            when (action) {
                is SearchAction.DateChanged -> viewModel.onDateChanged(action.value)
                is SearchAction.ModeSelected -> viewModel.onModeSelected(action.mode)
                is SearchAction.SelectStation -> onStationSearch(StationSearchRoute(action.field))
                SearchAction.SearchClicked -> viewModel.onSearchClick()
                is SearchAction.TimeChanged -> viewModel.onTimeChanged(action.value)
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreenContent(
    state: SearchUiState,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            val wideDateTimeFields = maxWidth >= 520.dp

            Column(
                modifier =
                    Modifier.fillMaxWidth()
                        .widthIn(max = 640.dp)
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.search_title),
                    style = MaterialTheme.typography.headlineMedium,
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth().testTag("search-mode")
                ) {
                    SearchMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = state.mode == mode,
                            onClick = { onAction(SearchAction.ModeSelected(mode)) },
                            shape =
                                SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = SearchMode.entries.size,
                                ),
                            label = { Text(stringResource(mode.optionLabelRes)) },
                        )
                    }
                }

                StationFields(
                    state = state,
                    onAction = onAction,
                )

                DateTimeFields(
                    state = state,
                    wide = wideDateTimeFields,
                    onAction = onAction,
                )

                Button(
                    onClick = { onAction(SearchAction.SearchClicked) },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("search-button"),
                ) {
                    Text(stringResource(R.string.search_button))
                }
            }
        }
    }
}

@Composable
private fun StationFields(
    state: SearchUiState,
    onAction: (SearchAction) -> Unit,
) {
    val targetStationErrorText = state.targetStationError?.message()

    StationButton(
        label = stringResource(state.mode.targetLabelRes),
        stationName = state.targetStationName,
        crsCode = state.targetStation,
        error = targetStationErrorText,
        testTag = "target-station",
        onClick = { onAction(SearchAction.SelectStation(StationField.Target)) },
    )

    StationButton(
        label = stringResource(state.mode.filterLabelRes),
        stationName = state.filterStationName,
        crsCode = state.filterStation,
        testTag = "filter-station",
        onClick = { onAction(SearchAction.SelectStation(StationField.Filter)) },
    )
}

@Composable
private fun StationButton(
    label: String,
    stationName: String,
    crsCode: String,
    testTag: String,
    onClick: () -> Unit,
    error: String? = null,
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag(testTag),
        ) {
            Text(
                if (stationName.isBlank()) stringResource(R.string.search_station_placeholder)
                else "$stationName ($crsCode)"
            )
        }
        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun DateTimeFields(
    state: SearchUiState,
    wide: Boolean,
    onAction: (SearchAction) -> Unit,
) {
    var activePicker by remember { mutableStateOf<DateTimePicker?>(null) }

    if (wide) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SelectableDateTimeText(
                label = stringResource(R.string.search_date_label),
                value = state.date,
                placeholder = stringResource(R.string.search_date_placeholder),
                error = state.dateTimeError,
                showErrorText = true,
                testTag = "search-date",
                onClick = { activePicker = DateTimePicker.Date },
                modifier = Modifier.weight(1f),
            )
            SelectableDateTimeText(
                label = stringResource(R.string.search_time_label),
                value = state.time,
                placeholder = stringResource(R.string.search_time_placeholder),
                error = state.dateTimeError,
                showErrorText = false,
                testTag = "search-time",
                onClick = { activePicker = DateTimePicker.Time },
                modifier = Modifier.weight(1f),
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SelectableDateTimeText(
                label = stringResource(R.string.search_date_label),
                value = state.date,
                placeholder = stringResource(R.string.search_date_placeholder),
                error = state.dateTimeError,
                showErrorText = true,
                testTag = "search-date",
                onClick = { activePicker = DateTimePicker.Date },
                modifier = Modifier.fillMaxWidth(),
            )
            SelectableDateTimeText(
                label = stringResource(R.string.search_time_label),
                value = state.time,
                placeholder = stringResource(R.string.search_time_placeholder),
                error = state.dateTimeError,
                showErrorText = false,
                testTag = "search-time",
                onClick = { activePicker = DateTimePicker.Time },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    when (activePicker) {
        DateTimePicker.Date -> {
            SearchDatePickerDialog(
                selectedDate = state.date,
                onDateSelected = { onAction(SearchAction.DateChanged(it)) },
                onDismiss = { activePicker = null },
            )
        }

        DateTimePicker.Time -> {
            SearchTimePickerDialog(
                selectedTime = state.time,
                onTimeSelected = { onAction(SearchAction.TimeChanged(it)) },
                onDismiss = { activePicker = null },
            )
        }

        null -> {}
    }
}

@Composable
private fun SelectableDateTimeText(
    label: String,
    value: String,
    placeholder: String,
    error: SearchValidationError?,
    showErrorText: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorText = error?.message()
    val valueText = value.ifBlank { placeholder }
    val textColor =
        when {
            errorText != null -> MaterialTheme.colorScheme.error
            value.isBlank() -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> MaterialTheme.colorScheme.primary
        }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color =
                if (errorText != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            modifier =
                Modifier.fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable(role = Role.Button, onClick = onClick)
                    .padding(vertical = 12.dp)
                    .testTag(testTag),
        )
        if (errorText != null && showErrorText) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchDatePickerDialog(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val selectedDateMillis = remember(selectedDate) { selectedDate.toDatePickerMillisOrNull() }

    key(selectedDateMillis) {
        val datePickerState =
            androidx.compose.material3.rememberDatePickerState(
                initialSelectedDateMillis = selectedDateMillis
            )

        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    enabled = datePickerState.selectedDateMillis != null,
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onDateSelected(formatDatePickerMillis(it))
                        }
                        onDismiss()
                    },
                ) {
                    Text(stringResource(R.string.search_picker_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.search_picker_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTimePickerDialog(
    selectedTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val (initialHour, initialMinute) = remember(selectedTime) { selectedTime.toHourMinute() }
    val timePickerState =
        androidx.compose.material3.rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true,
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(formatTime(timePickerState.hour, timePickerState.minute))
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.search_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.search_picker_cancel))
            }
        },
        text = { TimePicker(state = timePickerState) },
    )
}

private enum class DateTimePicker {
    Date,
    Time,
}

private fun String.toDatePickerMillisOrNull(): Long? =
    try {
        datePickerFormatter.parse(trim())?.time
    } catch (_: ParseException) {
        null
    }

private fun formatDatePickerMillis(millis: Long): String = datePickerFormatter.format(Date(millis))

private fun String.toHourMinute(): Pair<Int, Int> {
    val parts = trim().split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull()
    val minute = parts.getOrNull(1)?.toIntOrNull()

    return if (hour in 0..23 && minute in 0..59) {
        hour!! to minute!!
    } else {
        0 to 0
    }
}

private fun formatTime(
    hour: Int,
    minute: Int,
): String = String.format(Locale.UK, "%02d:%02d", hour, minute)

private val datePickerFormatter: SimpleDateFormat
    get() =
        SimpleDateFormat("yyyy-MM-dd", Locale.UK).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }

@Composable
internal fun DepartureBoardPlaceholderScreen(
    route: DepartureBoardRoute,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.search_departure_board_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = route.search.targetStation,
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 16.dp).width(160.dp),
        ) {
            Text(stringResource(R.string.search_back_button))
        }
    }
}

private val SearchMode.optionLabelRes: Int
    get() =
        when (this) {
            SearchMode.Departing -> R.string.search_mode_departing
            SearchMode.Arriving -> R.string.search_mode_arriving
        }

private val SearchMode.targetLabelRes: Int
    get() =
        when (this) {
            SearchMode.Departing -> R.string.search_target_departing_label
            SearchMode.Arriving -> R.string.search_target_arriving_label
        }

private val SearchMode.filterLabelRes: Int
    get() =
        when (this) {
            SearchMode.Departing -> R.string.search_filter_departing_label
            SearchMode.Arriving -> R.string.search_filter_arriving_label
        }

@Composable
private fun SearchValidationError.message(): String =
    when (this) {
        SearchValidationError.BlankStation -> stringResource(R.string.search_error_enter_station)
        SearchValidationError.InvalidDateTime ->
            stringResource(R.string.search_error_invalid_date_time)
    }

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
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

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun SearchScreenCompactPreview() {
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
