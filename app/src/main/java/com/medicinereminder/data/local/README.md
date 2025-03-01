# Database Migrations Guide

This document provides guidelines for managing database migrations in the Medicine Reminder application.

## Overview

The app uses Room database with versioned migrations. Any changes to database entities require:
1. Incrementing the database version
2. Creating a migration strategy
3. Adding tests to verify migration integrity

## Current Schema Version

The current database version is `3`. This is defined in `MedicineDatabase.kt` both in the `@Database` annotation and as `DB_VERSION` constant.

## Adding a New Migration

When you need to modify the database schema:

1. **Increment the version number** in `MedicineDatabase.kt`:
   ```kotlin
   @Database(
       entities = [MedicineEntity::class],
       version = X, // Increment this
       exportSchema = true
   )
   
   companion object {
       const val DATABASE_NAME = "medicine_db"
       const val DB_VERSION = X  // Update this too
   }
   ```

2. **Create a migration object** in `DatabaseModule.kt`:
   ```kotlin
   internal val MIGRATION_X_Y = object : Migration(X, Y) {
       override fun migrate(database: SupportSQLiteDatabase) {
           // Implement migration logic here
       }
   }
   ```

3. **Add the migration to the database builder**:
   ```kotlin
   .addMigrations(MIGRATION_1_2, MIGRATION_2_3, ..., MIGRATION_X_Y)
   ```

4. **Add test cases** in `MigrationTest.kt`:
   ```kotlin
   @Test
   @Throws(IOException::class)
   fun migrateXToY() {
       // Create version X of the database with sample data
       // Migrate to version Y
       // Verify data integrity
   }
   ```

5. **Update the ALL_MIGRATIONS array** in `MigrationTest.kt`:
   ```kotlin
   private val ALL_MIGRATIONS = arrayOf(
       DatabaseModule.MIGRATION_1_2,
       DatabaseModule.MIGRATION_2_3,
       ...,
       DatabaseModule.MIGRATION_X_Y
   )
   ```

## Best Practices

1. **Always preserve user data** - Migrations should never result in data loss.
2. **Test thoroughly** - Each migration must have tests verifying data integrity.
3. **Fallback strategy** - We use `.fallbackToDestructiveMigration()` as a last resort, but it will erase user data.
4. **Handle complex changes carefully** - SQLite limitations (cannot drop columns, etc.) may require table recreation.
5. **Document changes** - Comment each migration with a summary of schema changes.

## Running Migration Tests

Execute the following command to run migration tests:

```bash
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.medicinereminder.data.local.MigrationTest
```

This will verify that all migrations work correctly and preserve data integrity. 