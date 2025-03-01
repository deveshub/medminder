package com.medicinereminder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.medicinereminder.data.local.dao.MedicineDao
import com.medicinereminder.data.local.entity.MedicineEntity

@Database(
    entities = [MedicineEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MedicineDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao

    companion object {
        const val DATABASE_NAME = "medicine_db"
    }
} 