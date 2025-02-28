package com.medicinereminder.data.local.dao

import androidx.room.*
import com.medicinereminder.data.local.entity.MedicineEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface MedicineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity)

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: String): MedicineEntity?

    @Query("SELECT * FROM medicines ORDER BY name ASC")
    fun getAllMedicines(): Flow<List<MedicineEntity>>

    @Query("""
        SELECT * FROM medicines 
        WHERE startDate <= :to 
        AND (endDate IS NULL OR endDate >= :from)
        ORDER BY startDate ASC
    """)
    fun getMedicinesDueForReminder(from: LocalDateTime, to: LocalDateTime): Flow<List<MedicineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<MedicineEntity>)

    @Query("SELECT * FROM medicines")
    suspend fun getAllMedicinesForExport(): List<MedicineEntity>
} 