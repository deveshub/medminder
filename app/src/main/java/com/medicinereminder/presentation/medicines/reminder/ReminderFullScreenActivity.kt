package com.medicinereminder.presentation.medicines.reminder

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.medicinereminder.data.receiver.ReminderReceiver
import com.medicinereminder.presentation.theme.MedicineReminderTheme

class ReminderFullScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val medicineId = intent.getStringExtra("medicineId") ?: return finish()
        val medicineName = intent.getStringExtra("medicineName") ?: return finish()
        val notificationId = intent.getIntExtra("notificationId", 0)
        val snoozeRequestCode = intent.getIntExtra("snoozeRequestCode", 0)
        val maxSnoozeCount = intent.getIntExtra("maxSnoozeCount", 3)
        val snoozeInterval = intent.getIntExtra("snoozeInterval", 5)
        val soundEnabled = intent.getBooleanExtra("soundEnabled", true)
        val vibrationEnabled = intent.getBooleanExtra("vibrationEnabled", true)
        val currentSnoozeCount = intent.getIntExtra("currentSnoozeCount", 0)

        setContent {
            MedicineReminderTheme {
                ReminderDialog(
                    medicineName = medicineName,
                    onDismiss = { finish() },
                    onSnooze = {
                        if (currentSnoozeCount < maxSnoozeCount) {
                            val intent = Intent(this, ReminderReceiver::class.java).apply {
                                action = "com.medicinereminder.SNOOZE"
                                putExtra("medicineId", medicineId)
                                putExtra("medicineName", medicineName)
                                putExtra("notificationId", notificationId)
                                putExtra("snoozeRequestCode", snoozeRequestCode)
                                putExtra("isFullScreen", true)
                                putExtra("maxSnoozeCount", maxSnoozeCount)
                                putExtra("snoozeInterval", snoozeInterval)
                                putExtra("soundEnabled", soundEnabled)
                                putExtra("vibrationEnabled", vibrationEnabled)
                                putExtra("currentSnoozeCount", currentSnoozeCount)
                            }
                            sendBroadcast(intent)
                        }
                        finish()
                    },
                    onTake = {
                        val intent = Intent(this, ReminderReceiver::class.java).apply {
                            action = "com.medicinereminder.TAKE"
                            putExtra("medicineId", medicineId)
                            putExtra("notificationId", notificationId)
                        }
                        sendBroadcast(intent)
                        finish()
                    },
                    onSkip = {
                        val intent = Intent(this, ReminderReceiver::class.java).apply {
                            action = "com.medicinereminder.SKIP"
                            putExtra("medicineId", medicineId)
                            putExtra("notificationId", notificationId)
                        }
                        sendBroadcast(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun ReminderDialog(
    medicineName: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    onTake: () -> Unit,
    onSkip: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Medicine Reminder",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Time to take $medicineName",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onSnooze,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Snooze")
                    }
                    
                    Button(
                        onClick = onTake,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Take")
                    }
                    
                    Button(
                        onClick = onSkip,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Skip")
                    }
                }
            }
        }
    }
} 