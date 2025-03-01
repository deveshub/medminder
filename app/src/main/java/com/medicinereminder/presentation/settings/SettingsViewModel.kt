package com.medicinereminder.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicinereminder.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val defaultSnoozeInterval: Int = 30
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getDefaultSnoozeInterval()
                .catch { emit(30) }
                .collect { interval ->
                    _state.update { it.copy(defaultSnoozeInterval = interval) }
                }
        }
    }

    fun updateDefaultSnoozeInterval(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultSnoozeInterval(minutes)
        }
    }
} 