package com.medicinereminder.presentation.medicines.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicinereminder.domain.model.Dosage
import com.medicinereminder.domain.model.Medicine
import com.medicinereminder.domain.model.ReminderSettings
import com.medicinereminder.domain.model.Schedule
import com.medicinereminder.domain.usecase.AddMedicineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddMedicineViewModel @Inject constructor(
    private val addMedicineUseCase: AddMedicineUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddMedicineState())
    val state: StateFlow<AddMedicineState> = _state

    fun onEvent(event: AddMedicineEvent) {
        when (event) {
            is AddMedicineEvent.OnNameChange -> {
                _state.update { it.copy(
                    name = event.name,
                    isNameError = false
                ) }
            }
            is AddMedicineEvent.OnDosageAmountChange -> {
                _state.update { it.copy(
                    dosageAmount = event.amount,
                    isDosageError = false
                ) }
            }
            is AddMedicineEvent.OnDosageUnitChange -> {
                _state.update { it.copy(dosageUnit = event.unit) }
            }
            is AddMedicineEvent.OnFrequencyChange -> {
                _state.update { it.copy(frequency = event.frequency) }
            }
            is AddMedicineEvent.OnDayToggle -> {
                _state.update {
                    val days = it.selectedDays.toMutableSet()
                    if (event.day in days) {
                        days.remove(event.day)
                    } else {
                        days.add(event.day)
                    }
                    it.copy(selectedDays = days)
                }
            }
            is AddMedicineEvent.OnTimeAdd -> {
                _state.update {
                    val times = it.times.toMutableList()
                    if (event.time !in times) {
                        times.add(event.time)
                    }
                    it.copy(
                        times = times.sorted(),
                        isTimesError = false
                    )
                }
            }
            is AddMedicineEvent.OnTimeRemove -> {
                _state.update {
                    val times = it.times.toMutableList()
                    times.remove(event.time)
                    it.copy(times = times)
                }
            }
            is AddMedicineEvent.OnInstructionsChange -> {
                _state.update { it.copy(instructions = event.instructions) }
            }
            is AddMedicineEvent.OnStartDateChange -> {
                _state.update { it.copy(startDate = event.date) }
            }
            is AddMedicineEvent.OnEndDateChange -> {
                _state.update { it.copy(endDate = event.date) }
            }
            is AddMedicineEvent.OnReminderEnabledChange -> {
                _state.update { it.copy(isReminderEnabled = event.enabled) }
            }
            is AddMedicineEvent.OnSoundEnabledChange -> {
                _state.update { it.copy(isSoundEnabled = event.enabled) }
            }
            is AddMedicineEvent.OnVibrationEnabledChange -> {
                _state.update { it.copy(isVibrationEnabled = event.enabled) }
            }
            is AddMedicineEvent.OnFullScreenAlertChange -> {
                _state.update { it.copy(isFullScreenAlert = event.enabled) }
            }
            is AddMedicineEvent.OnSnoozeIntervalChange -> {
                _state.update { it.copy(snoozeInterval = event.interval) }
            }
            is AddMedicineEvent.OnMaxSnoozeCountChange -> {
                _state.update { it.copy(maxSnoozeCount = event.count) }
            }
            AddMedicineEvent.OnSave -> saveMedicine()
        }
    }

    private fun saveMedicine() {
        val currentState = state.value

        // Validate input
        var hasError = false
        if (currentState.name.isBlank()) {
            _state.update { it.copy(isNameError = true) }
            hasError = true
        }
        if (currentState.dosageAmount.isBlank() || currentState.dosageAmount.toDoubleOrNull() == null) {
            _state.update { it.copy(isDosageError = true) }
            hasError = true
        }
        if (currentState.times.isEmpty()) {
            _state.update { it.copy(isTimesError = true) }
            hasError = true
        }
        if (hasError) return

        val medicine = Medicine(
            id = UUID.randomUUID(),
            name = currentState.name,
            dosage = Dosage(
                amount = currentState.dosageAmount.toDouble(),
                unit = currentState.dosageUnit
            ),
            schedule = Schedule(
                frequency = currentState.frequency,
                times = currentState.times,
                daysOfWeek = if (currentState.selectedDays.isNotEmpty()) {
                    currentState.selectedDays.map { 
                        when (it) {
                            1 -> com.medicinereminder.domain.model.DayOfWeek.MONDAY
                            2 -> com.medicinereminder.domain.model.DayOfWeek.TUESDAY
                            3 -> com.medicinereminder.domain.model.DayOfWeek.WEDNESDAY
                            4 -> com.medicinereminder.domain.model.DayOfWeek.THURSDAY
                            5 -> com.medicinereminder.domain.model.DayOfWeek.FRIDAY
                            6 -> com.medicinereminder.domain.model.DayOfWeek.SATURDAY
                            7 -> com.medicinereminder.domain.model.DayOfWeek.SUNDAY
                            else -> throw IllegalArgumentException("Invalid day: $it")
                        }
                    }.toSet()
                } else null
            ),
            instructions = currentState.instructions.ifBlank { null },
            startDate = currentState.startDate,
            endDate = currentState.endDate,
            reminderSettings = ReminderSettings(
                enabled = currentState.isReminderEnabled,
                soundEnabled = currentState.isSoundEnabled,
                vibrationEnabled = currentState.isVibrationEnabled,
                fullScreenAlert = currentState.isFullScreenAlert,
                snoozeInterval = currentState.snoozeInterval,
                maxSnoozeCount = currentState.maxSnoozeCount
            )
        )

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                addMedicineUseCase(medicine)
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to save medicine"
                    )
                }
            }
        }
    }
} 