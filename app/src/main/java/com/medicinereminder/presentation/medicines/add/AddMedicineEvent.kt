package com.medicinereminder.presentation.medicines.add

import com.medicinereminder.domain.model.DosageUnit
import com.medicinereminder.domain.model.Frequency
import java.time.LocalDateTime

sealed class AddMedicineEvent {
    data class OnNameChange(val name: String) : AddMedicineEvent()
    data class OnDosageAmountChange(val amount: String) : AddMedicineEvent()
    data class OnDosageUnitChange(val unit: DosageUnit) : AddMedicineEvent()
    data class OnFrequencyChange(val frequency: Frequency) : AddMedicineEvent()
    data class OnDayToggle(val day: Int) : AddMedicineEvent()
    data class OnTimeAdd(val time: LocalDateTime) : AddMedicineEvent()
    data class OnTimeRemove(val time: LocalDateTime) : AddMedicineEvent()
    data class OnInstructionsChange(val instructions: String) : AddMedicineEvent()
    data class OnStartDateChange(val date: LocalDateTime) : AddMedicineEvent()
    data class OnEndDateChange(val date: LocalDateTime?) : AddMedicineEvent()
    data class OnReminderEnabledChange(val enabled: Boolean) : AddMedicineEvent()
    data class OnSoundEnabledChange(val enabled: Boolean) : AddMedicineEvent()
    data class OnVibrationEnabledChange(val enabled: Boolean) : AddMedicineEvent()
    data class OnFullScreenAlertChange(val enabled: Boolean) : AddMedicineEvent()
    object OnSave : AddMedicineEvent()
} 