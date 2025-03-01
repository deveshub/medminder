package com.medicinereminder.presentation.medicines.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicinereminder.data.manager.ReminderManager
import com.medicinereminder.domain.repository.MedicineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MedicineDetailsViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val reminderManager: ReminderManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(MedicineDetailsState())
    val state: StateFlow<MedicineDetailsState> = _state

    init {
        savedStateHandle.get<String>("medicineId")?.let { medicineId ->
            loadMedicine(medicineId)
        }
    }

    private fun loadMedicine(medicineId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val medicine = medicineRepository.getMedicineById(UUID.fromString(medicineId))
                _state.update {
                    it.copy(
                        medicine = medicine,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load medicine"
                    )
                }
            }
        }
    }

    fun deleteMedicine() {
        val medicine = state.value.medicine ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                medicineRepository.deleteMedicine(medicine.id)
                reminderManager.cancelReminder(medicine.id.toString())
                _state.update { it.copy(isLoading = false, isDeleted = true) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to delete medicine"
                    )
                }
            }
        }
    }
} 