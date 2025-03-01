package com.medicinereminder.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.medicinereminder.data.local.MedicineDatabase
import com.medicinereminder.data.local.dao.MedicineDao
import com.medicinereminder.data.repository.MedicineRepositoryImpl
import com.medicinereminder.domain.repository.MedicineRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // First add the new columns to the existing table
            database.execSQL("""
                ALTER TABLE medicines 
                ADD COLUMN status TEXT NOT NULL DEFAULT 'PENDING'
            """)
            
            database.execSQL("""
                ALTER TABLE medicines 
                ADD COLUMN lastStatusUpdate INTEGER DEFAULT NULL
            """)

            // Create the index on status
            database.execSQL("CREATE INDEX IF NOT EXISTS index_medicines_status ON medicines(status)")
        }
    }

    @Provides
    @Singleton
    fun provideMedicineDatabase(
        @ApplicationContext context: Context
    ): MedicineDatabase = Room.databaseBuilder(
            context,
            MedicineDatabase::class.java,
            MedicineDatabase.DATABASE_NAME
        )
        .addMigrations(MIGRATION_1_2)
        .build()

    @Provides
    @Singleton
    fun provideMedicineDao(database: MedicineDatabase): MedicineDao = database.medicineDao()

    @Provides
    @Singleton
    fun provideMedicineRepository(dao: MedicineDao): MedicineRepository = MedicineRepositoryImpl(dao)
} 