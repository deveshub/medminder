package com.medicinereminder.domain.repository

import com.medicinereminder.domain.model.Medicine
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.util.UUID

interface MedicineRepository {
    suspend fun addMedicine(medicine: Medicine)
    suspend fun updateMedicine(medicine: Medicine)
    suspend fun deleteMedicine(id: UUID)
    suspend fun getMedicineById(id: UUID): Medicine?
    fun getAllMedicines(): Flow<List<Medicine>>
    fun getMedicinesDueForReminder(from: LocalDateTime, to: LocalDateTime): Flow<List<Medicine>>
    suspend fun importMedicines(medicines: List<Medicine>)
    suspend fun exportMedicines(): List<Medicine>
} 