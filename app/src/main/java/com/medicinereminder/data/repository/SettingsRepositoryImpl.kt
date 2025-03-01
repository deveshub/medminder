package com.medicinereminder.data.repository

import android.content.Context
import com.medicinereminder.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    // For simplicity, using an in-memory StateFlow instead of DataStore
    private val defaultSnoozeIntervalFlow = MutableStateFlow(15) // Default 15 minutes

    override suspend fun setDefaultSnoozeInterval(minutes: Int) {
        defaultSnoozeIntervalFlow.emit(minutes)
    }

    override fun getDefaultSnoozeInterval(): Flow<Int> {
        return defaultSnoozeIntervalFlow
    }
} 