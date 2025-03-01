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
        val soundEnabled = intent.getBooleanExtra("soundEnabled", true)
        val vibrationEnabled = intent.getBooleanExtra("vibrationEnabled", true)

        setContent {
            MedicineReminderTheme {
                ReminderDialog(
                    medicineName = medicineName,
                    onDismiss = { finish() },
                    onSnooze = { minutes ->
                        val intent = Intent(this, ReminderReceiver::class.java).apply {
                            action = "com.medicinereminder.SNOOZE"
                            putExtra("medicineId", medicineId)
                            putExtra("medicineName", medicineName)
                            putExtra("notificationId", notificationId)
                            putExtra("snoozeRequestCode", snoozeRequestCode)
                            putExtra("isFullScreen", true)
                            putExtra("soundEnabled", soundEnabled)
                            putExtra("vibrationEnabled", vibrationEnabled)
                            putExtra("customSnoozeMinutes", minutes)
                        }
                        sendBroadcast(intent)
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
                            putExtra("soundEnabled", soundEnabled)
                            putExtra("vibrationEnabled", vibrationEnabled)
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
                            putExtra("soundEnabled", soundEnabled)
                            putExtra("vibrationEnabled", vibrationEnabled)
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
                        text = "Time to take",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = medicineName,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { showSnoozeOptions = true }
                        ) {
                            Text("Snooze")
                        }
                        Button(
                            onClick = onTake
                        ) {
                            Text("Take")
                        }
                        Button(
                            onClick = onSkip
                        ) {
                            Text("Skip")
                        }
                    }
                }

                if (showSnoozeOptions) {
                    Dialog(
                        onDismissRequest = { showSnoozeOptions = false }
                    ) {
                        Surface(
                            modifier = Modifier.padding(16.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Snooze for",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                snoozeOptions.forEach { (minutes, label) ->
                                    Button(
                                        onClick = {
                                            onSnooze(minutes)
                                            showSnoozeOptions = false
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(label)
                                    }
                                }
                                Button(
                                    onClick = {
                                        showSnoozeOptions = false
                                        showCustomSnoozeDialog = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text("Custom")
                                }
                            }
                        }
                    }
                }

                if (showCustomSnoozeDialog) {
                    Dialog(
                        onDismissRequest = { showCustomSnoozeDialog = false }
                    ) {
                        Surface(
                            modifier = Modifier.padding(16.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Enter minutes",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                OutlinedTextField(
                                    value = customSnoozeMinutes,
                                    onValueChange = { customSnoozeMinutes = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { showCustomSnoozeDialog = false }
                                    ) {
                                        Text("Cancel")
                                    }
                                    TextButton(
                                        onClick = {
                                            val minutes = customSnoozeMinutes.toIntOrNull()
                                            if (minutes != null && minutes > 0) {
                                                onSnooze(minutes)
                                                showCustomSnoozeDialog = false
                                            }
                                        }
                                    ) {
                                        Text("OK")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 