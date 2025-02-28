package com.medicinereminder.data.repository

import com.medicinereminder.data.local.dao.MedicineDao
import com.medicinereminder.data.local.entity.MedicineEntity
import com.medicinereminder.domain.model.Medicine
import com.medicinereminder.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class MedicineRepositoryImpl @Inject constructor(
    private val medicineDao: MedicineDao
) : MedicineRepository {

    override suspend fun addMedicine(medicine: Medicine) {
        medicineDao.insertMedicine(MedicineEntity.fromDomainModel(medicine))
    }

    override suspend fun updateMedicine(medicine: Medicine) {
        medicineDao.updateMedicine(MedicineEntity.fromDomainModel(medicine))
    }

    override suspend fun deleteMedicine(id: UUID) {
        medicineDao.getMedicineById(id.toString())?.let { 
            medicineDao.deleteMedicine(it)
        }
    }

    override suspend fun getMedicineById(id: UUID): Medicine? {
        return medicineDao.getMedicineById(id.toString())?.toDomainModel()
    }

    override fun getAllMedicines(): Flow<List<Medicine>> {
        return medicineDao.getAllMedicines().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getMedicinesDueForReminder(
        from: LocalDateTime,
        to: LocalDateTime
    ): Flow<List<Medicine>> {
        return medicineDao.getMedicinesDueForReminder(from, to).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun importMedicines(medicines: List<Medicine>) {
        val entities = medicines.map { MedicineEntity.fromDomainModel(it) }
        medicineDao.insertMedicines(entities)
    }

    override suspend fun exportMedicines(): List<Medicine> {
        return medicineDao.getAllMedicinesForExport().map { it.toDomainModel() }
    }
} 