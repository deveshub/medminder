package com.medicinereminder.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.medicinereminder.domain.model.*
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "medicines",
    indices = [
        Index(value = ["status"])
    ]
)
data class MedicineEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val dosageAmount: Double,
    val dosageUnit: String,
    val frequency: String,
    val scheduleTimes: List<LocalDateTime>,
    val scheduleDaysOfWeek: Set<String>?,
    val scheduleInterval: Int,
    val instructions: String?,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime?,
    val reminderEnabled: Boolean,
    val reminderSoundEnabled: Boolean,
    val reminderVibrationEnabled: Boolean,
    val reminderFullScreenAlert: Boolean,
    val reminderSnoozeInterval: Int,
    val reminderMaxSnoozeCount: Int,
    val status: String = MedicineStatus.PENDING.name,
    val lastStatusUpdate: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    fun toDomainModel(): Medicine = Medicine(
        id = UUID.fromString(id),
        name = name,
        dosage = Dosage(
            amount = dosageAmount,
            unit = DosageUnit.valueOf(dosageUnit)
        ),
        schedule = Schedule(
            frequency = Frequency.valueOf(frequency),
            times = scheduleTimes,
            daysOfWeek = scheduleDaysOfWeek?.map { DayOfWeek.valueOf(it) }?.toSet(),
            interval = scheduleInterval
        ),
        instructions = instructions,
        startDate = startDate,
        endDate = endDate,
        reminderSettings = ReminderSettings(
            enabled = reminderEnabled,
            soundEnabled = reminderSoundEnabled,
            vibrationEnabled = reminderVibrationEnabled,
            fullScreenAlert = reminderFullScreenAlert,
            snoozeInterval = reminderSnoozeInterval,
            maxSnoozeCount = reminderMaxSnoozeCount
        ),
        status = MedicineStatus.valueOf(status),
        lastStatusUpdate = lastStatusUpdate,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomainModel(medicine: Medicine): MedicineEntity = MedicineEntity(
            id = medicine.id.toString(),
            name = medicine.name,
            dosageAmount = medicine.dosage.amount,
            dosageUnit = medicine.dosage.unit.name,
            frequency = medicine.schedule.frequency.name,
            scheduleTimes = medicine.schedule.times,
            scheduleDaysOfWeek = medicine.schedule.daysOfWeek?.map { it.name }?.toSet(),
            scheduleInterval = medicine.schedule.interval,
            instructions = medicine.instructions,
            startDate = medicine.startDate,
            endDate = medicine.endDate,
            reminderEnabled = medicine.reminderSettings.enabled,
            reminderSoundEnabled = medicine.reminderSettings.soundEnabled,
            reminderVibrationEnabled = medicine.reminderSettings.vibrationEnabled,
            reminderFullScreenAlert = medicine.reminderSettings.fullScreenAlert,
            reminderSnoozeInterval = medicine.reminderSettings.snoozeInterval,
            reminderMaxSnoozeCount = medicine.reminderSettings.maxSnoozeCount,
            status = medicine.status.name,
            lastStatusUpdate = medicine.lastStatusUpdate,
            createdAt = medicine.createdAt,
            updatedAt = medicine.updatedAt
        )
    }
} 