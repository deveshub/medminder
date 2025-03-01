package com.medicinereminder.presentation.medicines.details

import com.medicinereminder.domain.model.Medicine

data class MedicineDetailsState(
    val medicine: Medicine? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false
) 