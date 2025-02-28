package com.medicinereminder.presentation.medicines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicinereminder.domain.model.Medicine
import com.medicinereminder.domain.repository.MedicineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicineListViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MedicineListState())
    val state: StateFlow<MedicineListState> = _state

    init {
        loadMedicines()
    }

    private fun loadMedicines() {
        _state.update { it.copy(isLoading = true) }
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
            .catch { throwable ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.message ?: "Unknown error occurred"
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
} 