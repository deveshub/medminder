package com.medicinereminder.presentation.medicines

import com.medicinereminder.domain.model.Medicine

data class MedicineListState(
    val medicines: List<Medicine> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 