package com.medicinereminder.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.medicinereminder.data.local.MedicineDatabase
import com.medicinereminder.data.local.dao.MedicineDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    internal val MIGRATION_1_2 = object : Migration(1, 2) {
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

    internal val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Since SQLite doesn't support dropping columns or changing column types directly,
            // we'll use a more comprehensive approach to recreate the table with the correct schema
            
            // 1. Rename the current table to a temporary table
            database.execSQL("ALTER TABLE medicines RENAME TO medicines_old")
            
            // 2. Create the new table with the updated schema
            database.execSQL("""
                CREATE TABLE medicines (
                    id TEXT PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    dosageAmount REAL NOT NULL,
                    dosageUnit TEXT NOT NULL,
                    frequency TEXT NOT NULL,
                    scheduleTimes TEXT NOT NULL,
                    scheduleDaysOfWeek TEXT,
                    scheduleInterval INTEGER NOT NULL,
                    instructions TEXT,
                    startDate INTEGER NOT NULL,
                    endDate INTEGER,
                    reminderEnabled INTEGER NOT NULL,
                    reminderSoundEnabled INTEGER NOT NULL,
                    reminderVibrationEnabled INTEGER NOT NULL,
                    reminderFullScreenAlert INTEGER NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    lastStatusUpdate INTEGER,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """)
            
            // 3. Copy data from the old table to the new one
            database.execSQL("""
                INSERT INTO medicines
                SELECT 
                    id, name, dosageAmount, dosageUnit, frequency, scheduleTimes, 
                    scheduleDaysOfWeek, scheduleInterval, instructions, startDate, 
                    endDate, reminderEnabled, reminderSoundEnabled, reminderVibrationEnabled, 
                    reminderFullScreenAlert, status, lastStatusUpdate, createdAt, updatedAt
                FROM medicines_old
            """)
            
            // 4. Drop the old table
            database.execSQL("DROP TABLE medicines_old")
            
            // 5. Recreate the index
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
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideMedicineDao(database: MedicineDatabase): MedicineDao = database.medicineDao()
} 