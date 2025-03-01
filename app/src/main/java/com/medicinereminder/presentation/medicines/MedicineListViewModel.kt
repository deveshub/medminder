package com.medicinereminder.presentation.medicines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicinereminder.domain.model.Medicine
import com.medicinereminder.domain.model.MedicineStatus
import com.medicinereminder.domain.repository.MedicineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MedicineListViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MedicineListState())
    val state: StateFlow<MedicineListState> = _state

    init {
        observeMedicines()
    }

    private fun observeMedicines() {
        medicineRepository.getAllMedicines()
            .onEach { medicines ->
                _state.update {
                    it.copy(
                        medicines = medicines,
                        isLoading = false,
                        error = null
                    )
                }
            }
            .catch { e ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load medicines"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            try {
                medicineRepository.deleteMedicine(medicine.id)
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Failed to delete medicine")
                }
            }
        }
    }

    fun updateMedicineStatus(medicineId: UUID, newStatus: MedicineStatus) {
        viewModelScope.launch {
            try {
                val medicine = medicineRepository.getMedicineById(medicineId)
                medicine?.let {
                    val updatedMedicine = it.copy(
                        status = newStatus,
                        lastStatusUpdate = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                    medicineRepository.updateMedicine(updatedMedicine)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Failed to update medicine status")
                }
            }
        }
    }
} 