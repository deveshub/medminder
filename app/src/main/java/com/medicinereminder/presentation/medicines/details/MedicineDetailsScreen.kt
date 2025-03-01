package com.medicinereminder.presentation.medicines.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: MedicineDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicine Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { state.medicine?.id?.toString()?.let { onNavigateToEdit(it) } }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Medicine")
                    }
                    IconButton(
                        onClick = { viewModel.deleteMedicine() }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Medicine")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            state.medicine?.let { medicine ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Basic Information
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Basic Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            DetailRow("Name", medicine.name)
                            DetailRow(
                                "Dosage",
                                "${medicine.dosage.amount} ${medicine.dosage.unit}"
                            )
                            if (!medicine.instructions.isNullOrBlank()) {
                                DetailRow("Instructions", medicine.instructions)
                            }
                        }
                    }

                    // Schedule Information
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Schedule",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            DetailRow("Frequency", medicine.schedule.frequency.name)
                            if (medicine.schedule.frequency == com.medicinereminder.domain.model.Frequency.WEEKLY) {
                                DetailRow(
                                    "Days",
                                    medicine.schedule.daysOfWeek?.joinToString(", ") { it.name } ?: "None"
                                )
                            }
                            Text("Times:")
                            medicine.schedule.times.forEach { time ->
                                Text(
                                    text = "• ${time.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                            DetailRow(
                                "Start Date",
                                medicine.startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                            )
                            medicine.endDate?.let {
                                DetailRow(
                                    "End Date",
                                    it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                                )
                            }
                        }
                    }

                    // Reminder Settings
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Reminder Settings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            DetailRow(
                                "Reminders Enabled",
                                if (medicine.reminderSettings.enabled) "Yes" else "No"
                            )
                            if (medicine.reminderSettings.enabled) {
                                DetailRow(
                                    "Sound",
                                    if (medicine.reminderSettings.soundEnabled) "On" else "Off"
                                )
                                DetailRow(
                                    "Vibration",
                                    if (medicine.reminderSettings.vibrationEnabled) "On" else "Off"
                                )
                                DetailRow(
                                    "Full Screen Alert",
                                    if (medicine.reminderSettings.fullScreenAlert) "On" else "Off"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
} 