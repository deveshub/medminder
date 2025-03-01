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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.medicinereminder.data.receiver.ReminderReceiver
import com.medicinereminder.presentation.theme.MedicineReminderTheme
import androidx.compose.foundation.text.KeyboardOptions

class ReminderFullScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        
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
                    onSnooze = { minutes ->
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
                                putExtra("customSnoozeMinutes", minutes)
                            }
                            sendBroadcast(intent)
                        }
                        finish()
                    },
                    onTake = {
                        val intent = Intent(this, ReminderReceiver::class.java).apply {
                            action = "com.medicinereminder.TAKE"
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
                        finish()
                    },
                    onSkip = {
                        val intent = Intent(this, ReminderReceiver::class.java).apply {
                            action = "com.medicinereminder.SKIP"
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
    onSnooze: (Int) -> Unit,
    onTake: () -> Unit,
    onSkip: () -> Unit
) {
    var showSnoozeOptions by remember { mutableStateOf(false) }
    val snoozeOptions = listOf(
        5 to "5 minutes",
        15 to "15 minutes",
        30 to "30 minutes",
        60 to "1 hour",
        120 to "2 hours"
    )
    var customSnoozeMinutes by remember { mutableStateOf("") }
    var showCustomSnoozeDialog by remember { mutableStateOf(false) }

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
            Box(modifier = Modifier.fillMaxSize()) {
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
                        Box {
                            Button(
                                onClick = { showSnoozeOptions = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("Snooze")
                            }
                            DropdownMenu(
                                expanded = showSnoozeOptions,
                                onDismissRequest = { showSnoozeOptions = false }
                            ) {
                                snoozeOptions.forEach { (minutes, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            showSnoozeOptions = false
                                            onSnooze(minutes)
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Custom...") },
                                    onClick = {
                                        showSnoozeOptions = false
                                        showCustomSnoozeDialog = true
                                    }
                                )
                            }
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

                if (showCustomSnoozeDialog) {
                    AlertDialog(
                        onDismissRequest = { showCustomSnoozeDialog = false },
                        title = { Text("Custom Snooze Time") },
                        text = {
                            OutlinedTextField(
                                value = customSnoozeMinutes,
                                onValueChange = { customSnoozeMinutes = it.filter { char -> char.isDigit() } },
                                label = { Text("Minutes") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showCustomSnoozeDialog = false
                                    if (customSnoozeMinutes.isNotEmpty()) {
                                        onSnooze(customSnoozeMinutes.toInt())
                                    }
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCustomSnoozeDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
} 