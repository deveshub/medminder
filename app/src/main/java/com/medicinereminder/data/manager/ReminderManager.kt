package com.medicinereminder.data.manager

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.medicinereminder.R
import com.medicinereminder.data.receiver.ReminderReceiver
import com.medicinereminder.domain.model.Medicine
import com.medicinereminder.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService<NotificationManager>()
    private val alarmManager = context.getSystemService<AlarmManager>()

    companion object {
        private const val CHANNEL_ID = "medicine_reminders"
        private const val CHANNEL_NAME = "Medicine Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications for medicine reminders"
        private const val NOTIFICATION_ID_PREFIX = 1000
        private const val ALARM_REQUEST_CODE_PREFIX = 2000
        private const val SNOOZE_REQUEST_CODE_PREFIX = 3000
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun scheduleReminder(medicine: Medicine, time: LocalDateTime) {
        val notificationId = NOTIFICATION_ID_PREFIX + medicine.id.hashCode()
        val alarmRequestCode = ALARM_REQUEST_CODE_PREFIX + medicine.id.hashCode()
        val snoozeRequestCode = SNOOZE_REQUEST_CODE_PREFIX + medicine.id.hashCode()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("medicineId", medicine.id.toString())
            putExtra("medicineName", medicine.name)
            putExtra("notificationId", notificationId)
            putExtra("snoozeRequestCode", snoozeRequestCode)
            putExtra("isFullScreen", medicine.reminderSettings.fullScreenAlert)
            putExtra("soundEnabled", medicine.reminderSettings.soundEnabled)
            putExtra("vibrationEnabled", medicine.reminderSettings.vibrationEnabled)
            putExtra("currentSnoozeCount", 0)
            flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = time
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        alarmManager?.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAtMillis, getPendingActivityIntent()),
            pendingIntent
        )
    }

    fun cancelReminder(medicineId: String) {
        val alarmRequestCode = ALARM_REQUEST_CODE_PREFIX + medicineId.hashCode()
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmRequestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager?.cancel(it)
            it.cancel()
        }
    }

    private fun getPendingActivityIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }
} 