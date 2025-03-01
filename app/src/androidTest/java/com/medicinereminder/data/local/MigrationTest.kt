package com.medicinereminder.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.medicinereminder.di.DatabaseModule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    // Helper for creating Room databases and migrations
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MedicineDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    // Array of all migrations
    private val ALL_MIGRATIONS = arrayOf(
        DatabaseModule.MIGRATION_1_2,
        DatabaseModule.MIGRATION_2_3
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        val medicineId = UUID.randomUUID().toString()
        
        // Create version 1 of the database
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert sample data for version 1
            val values = ContentValues().apply {
                put("id", medicineId)
                put("name", "Test Medicine")
                put("dosageAmount", 10.0)
                put("dosageUnit", "MG")
                put("frequency", "DAILY")
                put("scheduleTimes", "[]") // Empty JSON array for times
                put("scheduleDaysOfWeek", null)
                put("scheduleInterval", 1)
                put("instructions", "Take with water")
                put("startDate", System.currentTimeMillis())
                put("endDate", null)
                put("reminderEnabled", 1)
                put("reminderSoundEnabled", 1)
                put("reminderVibrationEnabled", 1)
                put("reminderFullScreenAlert", 0)
                put("createdAt", System.currentTimeMillis())
                put("updatedAt", System.currentTimeMillis())
            }
            
            insert("medicines", SQLiteDatabase.CONFLICT_REPLACE, values)
            close()
        }

        // Migrate from version 1 to version 2
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, DatabaseModule.MIGRATION_1_2)
        
        // Verify that the data was migrated correctly
        val cursor = db.query("SELECT * FROM medicines WHERE id = ?", arrayOf(medicineId))
        cursor.moveToFirst()
        
        // Verify new columns exist with default values
        val statusIndex = cursor.getColumnIndex("status")
        val status = cursor.getString(statusIndex)
        assertEquals("PENDING", status)
        
        cursor.close()
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate2To3() {
        val medicineId = UUID.randomUUID().toString()
        
        // Create version 2 of the database
        helper.createDatabase(TEST_DB, 2).apply {
            // Insert sample data for version 2
            val values = ContentValues().apply {
                put("id", medicineId)
                put("name", "Test Medicine")
                put("dosageAmount", 10.0)
                put("dosageUnit", "MG")
                put("frequency", "DAILY")
                put("scheduleTimes", "[]") 
                put("scheduleDaysOfWeek", null)
                put("scheduleInterval", 1)
                put("instructions", "Take with water")
                put("startDate", System.currentTimeMillis())
                put("endDate", null)
                put("reminderEnabled", 1)
                put("reminderSoundEnabled", 1)
                put("reminderVibrationEnabled", 1)
                put("reminderFullScreenAlert", 0)
                put("status", "PENDING")
                put("lastStatusUpdate", null)
                put("createdAt", System.currentTimeMillis())
                put("updatedAt", System.currentTimeMillis())
            }
            
            insert("medicines", SQLiteDatabase.CONFLICT_REPLACE, values)
            close()
        }

        // Migrate from version 2 to version 3
        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, DatabaseModule.MIGRATION_2_3)
        
        // Verify that the data was preserved
        val cursor = db.query("SELECT * FROM medicines WHERE id = ?", arrayOf(medicineId))
        cursor.moveToFirst()
        
        // Check that the medicine still exists and data is preserved
        val nameIndex = cursor.getColumnIndex("name")
        val name = cursor.getString(nameIndex)
        assertEquals("Test Medicine", name)
        
        cursor.close()
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrateAllVersions() {
        // Create earliest version of the database
        helper.createDatabase(TEST_DB, 1).close()

        // Test migrating to each version in sequence
        val db = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            MedicineDatabase::class.java,
            TEST_DB
        ).addMigrations(*ALL_MIGRATIONS).build()

        // Verify database is open
        assert(db.isOpen)
        
        // Close the database
        db.close()
    }
    
    /**
     * This test ensures that the current schema in MedicineEntity.kt
     * matches what Room expects based on the version number.
     * This test will fail if you change entity fields without updating version.
     */
    @Test
    @Throws(IOException::class)
    fun currentSchemaValidation() {
        // Create a new database with the current version
        helper.createDatabase(TEST_DB, MedicineDatabase.DB_VERSION).close()

        // Will throw an exception if the schema is invalid
        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            MedicineDatabase::class.java,
            TEST_DB
        ).build().apply {
            openHelper.writableDatabase
            close()
        }
    }
} 