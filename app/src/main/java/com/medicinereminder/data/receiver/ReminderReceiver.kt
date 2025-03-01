package com.medicinereminder.data.receiver

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.work.*
import com.medicinereminder.R
import com.medicinereminder.data.worker.UpdateMedicineStatusWorker
import com.medicinereminder.domain.model.MedicineStatus
import com.medicinereminder.domain.repository.MedicineRepository
import com.medicinereminder.domain.repository.SettingsRepository
import com.medicinereminder.presentation.MainActivity
import com.medicinereminder.presentation.medicines.reminder.ReminderFullScreenActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var medicineRepository: MedicineRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    companion object {
        private const val CHANNEL_ID = "medicine_reminders"
        private const val SNOOZE_ACTION = "com.medicinereminder.SNOOZE"
        private const val TAKE_ACTION = "com.medicinereminder.TAKE"
        private const val SKIP_ACTION = "com.medicinereminder.SKIP"
        private const val EXTRA_MEDICINE_NAME = "medicineName"
        private const val EXTRA_MEDICINE_ID = "medicineId"
        private const val EXTRA_NOTIFICATION_ID = "notificationId"
        private const val UPDATE_STATUS_WORK = "update_status_work"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getStringExtra("medicineId") ?: return
        val medicineName = intent.getStringExtra("medicineName") ?: return
        val notificationId = intent.getIntExtra("notificationId", 0)
        val snoozeRequestCode = intent.getIntExtra("snoozeRequestCode", 0)
        val isFullScreen = intent.getBooleanExtra("isFullScreen", true)
        val soundEnabled = intent.getBooleanExtra("soundEnabled", true)
        val vibrationEnabled = intent.getBooleanExtra("vibrationEnabled", true)
        val customSnoozeMinutes = intent.getIntExtra("customSnoozeMinutes", -1)

        android.util.Log.d("ReminderReceiver", "Received action: ${intent.action} for medicine: $medicineId")

        when (intent.action) {
            SNOOZE_ACTION -> {
                val snoozeInterval = if (customSnoozeMinutes > 0) {
                    customSnoozeMinutes
                } else {
                    runBlocking {
                        settingsRepository.getDefaultSnoozeInterval().first()
                    }
                }
                scheduleSnooze(
                    context = context,
                    medicineId = medicineId,
                    medicineName = medicineName,
                    notificationId = notificationId,
                    snoozeRequestCode = snoozeRequestCode,
                    isFullScreen = isFullScreen,
                    soundEnabled = soundEnabled,
                    vibrationEnabled = vibrationEnabled,
                    snoozeInterval = snoozeInterval
                )
                scheduleStatusUpdate(context, medicineId, MedicineStatus.SNOOZED)
                cancelNotification(context, notificationId)
            }
            TAKE_ACTION -> {
                scheduleStatusUpdate(context, medicineId, MedicineStatus.TAKEN)
                cancelNotification(context, notificationId)
            }
            SKIP_ACTION -> {
                scheduleStatusUpdate(context, medicineId, MedicineStatus.SKIPPED)
                cancelNotification(context, notificationId)
            }
            else -> {
                showNotification(
                    context = context,
                    medicineId = medicineId,
                    medicineName = medicineName,
                    notificationId = notificationId,
                    snoozeRequestCode = snoozeRequestCode,
                    isFullScreen = isFullScreen,
                    soundEnabled = soundEnabled,
                    vibrationEnabled = vibrationEnabled
                )
            }
        }
    }

    private fun scheduleStatusUpdate(context: Context, medicineId: String, status: MedicineStatus) {
        val workRequest = OneTimeWorkRequestBuilder<UpdateMedicineStatusWorker>()
            .setInputData(
                workDataOf(
                    "medicineId" to medicineId,
                    "status" to status.name
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "$UPDATE_STATUS_WORK-${medicineId}-${UUID.randomUUID()}",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    private fun showNotification(
        context: Context,
        medicineId: String,
        medicineName: String,
        notificationId: Int,
        snoozeRequestCode: Int,
        isFullScreen: Boolean,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean
    ) {
        val notificationManager = context.getSystemService<NotificationManager>()

        if (isFullScreen) {
            showFullScreenAlert(context, Intent().apply {
                putExtra("medicineId", medicineId)
                putExtra("medicineName", medicineName)
                putExtra("notificationId", notificationId)
                putExtra("snoozeRequestCode", snoozeRequestCode)
                putExtra("soundEnabled", soundEnabled)
                putExtra("vibrationEnabled", vibrationEnabled)
            })
            return
        }

        val fullScreenIntent = Intent(context, ReminderFullScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("medicineId", medicineId)
            putExtra("medicineName", medicineName)
            putExtra("notificationId", notificationId)
            putExtra("snoozeRequestCode", snoozeRequestCode)
            putExtra("soundEnabled", soundEnabled)
            putExtra("vibrationEnabled", vibrationEnabled)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = SNOOZE_ACTION
            putExtra("medicineId", medicineId)
            putExtra("medicineName", medicineName)
            putExtra("notificationId", notificationId)
            putExtra("snoozeRequestCode", snoozeRequestCode)
            putExtra("isFullScreen", false)
            putExtra("soundEnabled", soundEnabled)
            putExtra("vibrationEnabled", vibrationEnabled)
        }

        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            snoozeRequestCode,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takeIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = TAKE_ACTION
            putExtra("medicineId", medicineId)
            putExtra("medicineName", medicineName)
            putExtra("notificationId", notificationId)
            putExtra("snoozeRequestCode", snoozeRequestCode)
            putExtra("isFullScreen", false)
            putExtra("soundEnabled", soundEnabled)
            putExtra("vibrationEnabled", vibrationEnabled)
        }

        val takePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val skipIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = SKIP_ACTION
            putExtra("medicineId", medicineId)
            putExtra("medicineName", medicineName)
            putExtra("notificationId", notificationId)
            putExtra("snoozeRequestCode", snoozeRequestCode)
            putExtra("isFullScreen", false)
            putExtra("soundEnabled", soundEnabled)
            putExtra("vibrationEnabled", vibrationEnabled)
        }

        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Medicine Reminder")
            .setContentText("Time to take $medicineName")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_snooze,
                "Snooze",
                snoozePendingIntent
            )
            .addAction(
                R.drawable.ic_check,
                "Take",
                takePendingIntent
            )
            .addAction(
                R.drawable.ic_skip,
                "Skip",
                skipPendingIntent
            )
            .build()

        notificationManager?.notify(notificationId, notification)
    }

    private fun showFullScreenAlert(context: Context, intent: Intent) {
        val fullScreenIntent = Intent(context, ReminderFullScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_MEDICINE_NAME, intent.getStringExtra("medicineName"))
            putExtra(EXTRA_MEDICINE_ID, intent.getStringExtra("medicineId"))
            putExtra(EXTRA_NOTIFICATION_ID, intent.getIntExtra("notificationId", 0))
        }

        context.startActivity(fullScreenIntent)
    }

    private fun scheduleSnooze(
        context: Context,
        medicineId: String,
        medicineName: String,
        notificationId: Int,
        snoozeRequestCode: Int,
        isFullScreen: Boolean,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
        snoozeInterval: Int
    ) {
        val alarmManager = context.getSystemService<AlarmManager>()
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("medicineId", medicineId)
            putExtra("medicineName", medicineName)
            putExtra("notificationId", notificationId)
            putExtra("snoozeRequestCode", snoozeRequestCode)
            putExtra("isFullScreen", isFullScreen)
            putExtra("soundEnabled", soundEnabled)
            putExtra("vibrationEnabled", vibrationEnabled)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            snoozeRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (snoozeInterval * 60 * 1000)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager?.canScheduleExactAlarms() == true) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTime, null),
                    pendingIntent
                )
            }
        } else {
            alarmManager?.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, null),
                pendingIntent
            )
        }
    }

    private fun cancelNotification(context: Context, notificationId: Int) {
        val notificationManager = context.getSystemService<NotificationManager>()
        notificationManager?.cancel(notificationId)
    }
} 