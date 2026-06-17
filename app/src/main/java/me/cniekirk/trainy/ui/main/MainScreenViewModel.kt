package me.cniekirk.trainy.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.cniekirk.trainy.data.DataRepository
import me.cniekirk.trainy.ui.main.MainScreenUiState.Success

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class MainScreenViewModel(dataRepository: DataRepository) : ViewModel() {
    val uiState: StateFlow<MainScreenUiState> =
        dataRepository.data
            .map<List<String>, MainScreenUiState>(::Success)
            .catch { emit(MainScreenUiState.Error(it)) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                MainScreenUiState.Loading,
            )
}

sealed interface MainScreenUiState {
    object Loading : MainScreenUiState

    data class Error(val throwable: Throwable) : MainScreenUiState

    data class Success(val data: List<String>) : MainScreenUiState
}
