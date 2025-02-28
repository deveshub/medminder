package com.medicinereminder.domain.usecase

import com.medicinereminder.domain.model.Medicine
import com.medicinereminder.domain.repository.MedicineRepository
import javax.inject.Inject

class AddMedicineUseCase @Inject constructor(
    private val repository: MedicineRepository
) {
    suspend operator fun invoke(medicine: Medicine) {
        require(medicine.name.isNotBlank()) { "Medicine name cannot be empty" }
        require(medicine.dosage.amount > 0) { "Dosage amount must be positive" }
        require(medicine.schedule.times.isNotEmpty()) { "Schedule must include at least one time" }
        
        repository.addMedicine(medicine)
    }
} 