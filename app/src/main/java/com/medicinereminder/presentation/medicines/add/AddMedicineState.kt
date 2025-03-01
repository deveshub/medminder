package com.medicinereminder.presentation.medicines.add

import com.medicinereminder.domain.model.DosageUnit
import com.medicinereminder.domain.model.Frequency
import java.time.LocalDateTime

data class AddMedicineState(
    val name: String = "",
    val dosageAmount: String = "",
    val dosageUnit: DosageUnit = DosageUnit.PILL,
    val frequency: Frequency = Frequency.DAILY,
    val selectedDays: Set<Int> = emptySet(), // 1 = Monday, 7 = Sunday
    val times: List<LocalDateTime> = emptyList(),
    val instructions: String = "",
    val startDate: LocalDateTime = LocalDateTime.now(),
    val endDate: LocalDateTime? = null,
    val isReminderEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val isFullScreenAlert: Boolean = false,
    val snoozeInterval: Int = 10,
    val maxSnoozeCount: Int = 3,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isNameError: Boolean = false,
    val isDosageError: Boolean = false,
    val isTimesError: Boolean = false
) 