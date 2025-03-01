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
import com.medicinereminder.R
import com.medicinereminder.presentation.MainActivity
import com.medicinereminder.presentation.medicines.reminder.ReminderFullScreenActivity

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "medicine_reminders"
        private const val SNOOZE_ACTION = "com.medicinereminder.SNOOZE"
        private const val TAKE_ACTION = "com.medicinereminder.TAKE"
        private const val SKIP_ACTION = "com.medicinereminder.SKIP"
        private const val EXTRA_MEDICINE_NAME = "medicineName"
        private const val EXTRA_MEDICINE_ID = "medicineId"
        private const val EXTRA_NOTIFICATION_ID = "notificationId"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getStringExtra("medicineId") ?: return
        val medicineName = intent.getStringExtra("medicineName") ?: return
        val notificationId = intent.getIntExtra("notificationId", 0)
        val snoozeRequestCode = intent.getIntExtra("snoozeRequestCode", 0)
        val isFullScreen = intent.getBooleanExtra("isFullScreen", false)
        val maxSnoozeCount = intent.getIntExtra("maxSnoozeCount", 3)
        val snoozeInterval = intent.getIntExtra("snoozeInterval", 5)
        val soundEnabled = intent.getBooleanExtra("soundEnabled", true)
        val vibrationEnabled = intent.getBooleanExtra("vibrationEnabled", true)
        val currentSnoozeCount = intent.getIntExtra("currentSnoozeCount", 0)
        val customSnoozeMinutes = intent.getIntExtra("customSnoozeMinutes", -1)

        when (intent.action) {
            SNOOZE_ACTION -> {
                if (currentSnoozeCount < maxSnoozeCount) {
                    scheduleSnooze(
                        context,
                        medicineId,
                        medicineName,
                        notificationId,
                        snoozeRequestCode,
                        isFullScreen,
                        maxSnoozeCount,
                        if (customSnoozeMinutes > 0) customSnoozeMinutes else snoozeInterval,
                        soundEnabled,
                        vibrationEnabled,
                        currentSnoozeCount + 1
                    )
                }
                cancelNotification(context, notificationId)
            }
            TAKE_ACTION -> {
                // TODO: Mark medicine as taken
                cancelNotification(context, notificationId)
            }
            SKIP_ACTION -> {
                // TODO: Mark medicine as skipped
                cancelNotification(context, notificationId)
            }
            else -> {
                if (isFullScreen) {
                    showFullScreenAlert(context, intent)
                }
                
                showNotification(
                    context,
                    medicineId,
                    medicineName,
                    notificationId,
                    snoozeRequestCode,
                    maxSnoozeCount,
                    snoozeInterval,
                    soundEnabled,
                    vibrationEnabled,
                    currentSnoozeCount
                )

                if (vibrationEnabled) {
                    vibrate(context)
                }

                if (soundEnabled) {
                    playSound(context)
                }
            }
        }
    }

    private fun showNotification(
        context: Context,
        medicineId: String,
        medicineName: String,
        notificationId: Int,
        snoozeRequestCode: Int,
        maxSnoozeCount: Int,
        snoozeInterval: Int,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
        currentSnoozeCount: Int
    ) {
        val notificationManager = context.getSystemService<NotificationManager>()

        val fullScreenIntent = Intent(context, ReminderFullScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("medicineId", medicineId)
            putExtra("medicineName", medicineName)
            putExtra("notificationId", notificationId)
            putExtra("snoozeRequestCode", snoozeRequestCode)
            putExtra("maxSnoozeCount", maxSnoozeCount)
            putExtra("snoozeInterval", snoozeInterval)
            putExtra("soundEnabled", soundEnabled)
            putExtra("vibrationEnabled", vibrationEnabled)
            putExtra("currentSnoozeCount", currentSnoozeCount)
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
            putExtra("maxSnoozeCount", maxSnoozeCount)
            putExtra("snoozeInterval", snoozeInterval)
            putExtra("soundEnabled", soundEnabled)
            putExtra("vibrationEnabled", vibrationEnabled)
            putExtra("currentSnoozeCount", currentSnoozeCount)
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
            putExtra("notificationId", notificationId)
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
            putExtra("notificationId", notificationId)
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
        maxSnoozeCount: Int,
        snoozeInterval: Int,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
        currentSnoozeCount: Int
    ) {
        val alarmManager = context.getSystemService<AlarmManager>()
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("medicineId", medicineId)
            putExtra("medicineName", medicineName)
            putExtra("notificationId", notificationId)
            putExtra("snoozeRequestCode", snoozeRequestCode)
            putExtra("isFullScreen", isFullScreen)
            putExtra("maxSnoozeCount", maxSnoozeCount)
            putExtra("snoozeInterval", snoozeInterval)
            putExtra("soundEnabled", soundEnabled)
            putExtra("vibrationEnabled", vibrationEnabled)
            putExtra("currentSnoozeCount", currentSnoozeCount)
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

    private fun vibrate(context: Context) {
        val vibrator = context.getSystemService<Vibrator>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(1000)
        }
    }

    private fun playSound(context: Context) {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone = RingtoneManager.getRingtone(context, notification)
            ringtone.play()
        } catch (e: Exception) {
            // Fallback to notification sound if alarm sound fails
            try {
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context, notification)
                ringtone.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
} 