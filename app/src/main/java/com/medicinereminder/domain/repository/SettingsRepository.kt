package com.medicinereminder.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getDefaultSnoozeInterval(): Flow<Int>
    suspend fun setDefaultSnoozeInterval(minutes: Int)
} 