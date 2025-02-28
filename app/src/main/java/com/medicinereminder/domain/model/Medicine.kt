package com.medicinereminder.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class Medicine(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val dosage: Dosage,
    val schedule: Schedule,
    val instructions: String?,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime?,
    val reminderSettings: ReminderSettings,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class Dosage(
    val amount: Double,
    val unit: DosageUnit
)

enum class DosageUnit {
    PILL, ML, MG, G, DROPS, PUFFS, UNITS
}

data class Schedule(
    val frequency: Frequency,
    val times: List<LocalDateTime>,
    val daysOfWeek: Set<DayOfWeek>? = null,
    val interval: Int = 1 // For frequencies like "every 2 days"
)

enum class Frequency {
    DAILY, WEEKLY, MONTHLY, AS_NEEDED, SPECIFIC_DAYS
}

enum class DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

data class ReminderSettings(
    val enabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val fullScreenAlert: Boolean = false,
    val snoozeInterval: Int = 10, // in minutes
    val maxSnoozeCount: Int = 3
) 