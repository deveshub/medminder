package com.medicinereminder.presentation.medicines.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medicinereminder.domain.model.DosageUnit
import com.medicinereminder.domain.model.Frequency
import com.medicinereminder.presentation.components.DatePickerDialog
import com.medicinereminder.presentation.components.TimePickerDialog
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddMedicineViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showDosageUnitMenu by remember { mutableStateOf(false) }
    var showFrequencyMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Medicine") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.onEvent(AddMedicineEvent.OnSave) },
                        enabled = !state.isLoading
                    ) {
                        Text("Save")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Medicine Name
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.onEvent(AddMedicineEvent.OnNameChange(it)) },
                    label = { Text("Medicine Name") },
                    isError = state.isNameError,
                    supportingText = if (state.isNameError) {
                        { Text("Name is required") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dosage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.dosageAmount,
                        onValueChange = { viewModel.onEvent(AddMedicineEvent.OnDosageAmountChange(it)) },
                        label = { Text("Dosage") },
                        isError = state.isDosageError,
                        supportingText = if (state.isDosageError) {
                            { Text("Enter valid dosage") }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )

                    ExposedDropdownMenuBox(
                        expanded = showDosageUnitMenu,
                        onExpandedChange = { showDosageUnitMenu = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = state.dosageUnit.name,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDosageUnitMenu)
                            },
                            modifier = Modifier.menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = showDosageUnitMenu,
                            onDismissRequest = { showDosageUnitMenu = false }
                        ) {
                            DosageUnit.values().forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit.name) },
                                    onClick = {
                                        viewModel.onEvent(AddMedicineEvent.OnDosageUnitChange(unit))
                                        showDosageUnitMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Frequency
                ExposedDropdownMenuBox(
                    expanded = showFrequencyMenu,
                    onExpandedChange = { showFrequencyMenu = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.frequency.name,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Frequency") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFrequencyMenu)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = showFrequencyMenu,
                        onDismissRequest = { showFrequencyMenu = false }
                    ) {
                        Frequency.values().forEach { frequency ->
                            DropdownMenuItem(
                                text = { Text(frequency.name) },
                                onClick = {
                                    viewModel.onEvent(AddMedicineEvent.OnFrequencyChange(frequency))
                                    showFrequencyMenu = false
                                }
                            )
                        }
                    }
                }

                // Days of Week (if weekly frequency)
                if (state.frequency == Frequency.WEEKLY) {
                    Text(
                        text = "Select Days",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { index, day ->
                            val isSelected = (index + 1) in state.selectedDays
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.onEvent(AddMedicineEvent.OnDayToggle(index + 1))
                                },
                                label = { Text(day) }
                            )
                        }
                    }
                }

                // Times
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Times",
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Time")
                        }
                    }
                    if (state.isTimesError) {
                        Text(
                            text = "At least one time is required",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    state.times.forEach { time ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(time.format(DateTimeFormatter.ofPattern("HH:mm")))
                            IconButton(
                                onClick = {
                                    viewModel.onEvent(AddMedicineEvent.OnTimeRemove(time))
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove Time")
                            }
                        }
                    }
                }

                // Start Date
                OutlinedTextField(
                    value = state.startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Start Date") },
                    trailingIcon = {
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // End Date
                OutlinedTextField(
                    value = state.endDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                        ?: "No End Date",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("End Date (Optional)") },
                    trailingIcon = {
                        IconButton(onClick = { showEndDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Instructions
                OutlinedTextField(
                    value = state.instructions,
                    onValueChange = { viewModel.onEvent(AddMedicineEvent.OnInstructionsChange(it)) },
                    label = { Text("Instructions (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

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
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Enable Reminders")
                            Switch(
                                checked = state.isReminderEnabled,
                                onCheckedChange = {
                                    viewModel.onEvent(AddMedicineEvent.OnReminderEnabledChange(it))
                                }
                            )
                        }

                        if (state.isReminderEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Sound")
                                Switch(
                                    checked = state.isSoundEnabled,
                                    onCheckedChange = {
                                        viewModel.onEvent(AddMedicineEvent.OnSoundEnabledChange(it))
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Vibration")
                                Switch(
                                    checked = state.isVibrationEnabled,
                                    onCheckedChange = {
                                        viewModel.onEvent(AddMedicineEvent.OnVibrationEnabledChange(it))
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Full Screen Alert")
                                Switch(
                                    checked = state.isFullScreenAlert,
                                    onCheckedChange = {
                                        viewModel.onEvent(AddMedicineEvent.OnFullScreenAlertChange(it))
                                    }
                                )
                            }

                            Text(
                                text = "Snooze Interval (minutes)",
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Slider(
                                value = state.snoozeInterval.toFloat(),
                                onValueChange = {
                                    viewModel.onEvent(AddMedicineEvent.OnSnoozeIntervalChange(it.toInt()))
                                },
                                valueRange = 1f..30f,
                                steps = 29
                            )
                            Text(
                                text = "${state.snoozeInterval} minutes",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Max Snooze Count",
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Slider(
                                value = state.maxSnoozeCount.toFloat(),
                                onValueChange = {
                                    viewModel.onEvent(AddMedicineEvent.OnMaxSnoozeCountChange(it.toInt()))
                                },
                                valueRange = 0f..5f,
                                steps = 5
                            )
                            Text(
                                text = "${state.maxSnoozeCount} times",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onTimeSelected = { time ->
                val dateTime = LocalDateTime.of(
                    state.startDate.toLocalDate(),
                    LocalTime.of(time.hour, time.minute)
                )
                viewModel.onEvent(AddMedicineEvent.OnTimeAdd(dateTime))
            }
        )
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            onDateSelected = { date ->
                viewModel.onEvent(
                    AddMedicineEvent.OnStartDateChange(
                        LocalDateTime.of(date, LocalTime.MIDNIGHT)
                    )
                )
            },
            initialDate = state.startDate.toLocalDate()
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            onDateSelected = { date ->
                viewModel.onEvent(
                    AddMedicineEvent.OnEndDateChange(
                        LocalDateTime.of(date, LocalTime.MIDNIGHT)
                    )
                )
            },
            initialDate = state.endDate?.toLocalDate() ?: state.startDate.toLocalDate()
        )
    }
} 