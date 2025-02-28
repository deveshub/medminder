package com.medicinereminder.di

import android.content.Context
import androidx.room.Room
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

    @Provides
    @Singleton
    fun provideMedicineDatabase(
        @ApplicationContext context: Context
    ): MedicineDatabase {
        return Room.databaseBuilder(
            context,
            MedicineDatabase::class.java,
            MedicineDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideMedicineDao(database: MedicineDatabase): MedicineDao {
        return database.medicineDao()
    }

    @Provides
    @Singleton
    fun provideMedicineRepository(
        medicineDao: MedicineDao
    ): MedicineRepository {
        return MedicineRepositoryImpl(medicineDao)
    }
} 