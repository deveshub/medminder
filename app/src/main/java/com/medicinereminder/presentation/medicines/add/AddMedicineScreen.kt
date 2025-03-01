package com.medicinereminder.presentation.medicines.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medicinereminder.domain.model.DosageUnit
import com.medicinereminder.domain.model.Frequency
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
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isStartDate by remember { mutableStateOf(true) }
    
    // Track dropdown expansion states
    var isUnitDropdownExpanded by remember { mutableStateOf(false) }
    var isFrequencyDropdownExpanded by remember { mutableStateOf(false) }

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
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Medicine Details",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.onEvent(AddMedicineEvent.OnNameChange(it)) },
                        label = { Text("Name") },
                        isError = state.isNameError,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.dosageAmount,
                            onValueChange = { viewModel.onEvent(AddMedicineEvent.OnDosageAmountChange(it)) },
                            label = { Text("Dosage") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = state.isDosageError,
                            modifier = Modifier.weight(1f)
                        )

                        ExposedDropdownMenuBox(
                            expanded = isUnitDropdownExpanded,
                            onExpandedChange = { isUnitDropdownExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = state.dosageUnit.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unit") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isUnitDropdownExpanded) },
                                modifier = Modifier.menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = isUnitDropdownExpanded,
                                onDismissRequest = { isUnitDropdownExpanded = false }
                            ) {
                                DosageUnit.values().forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit.name) },
                                        onClick = {
                                            viewModel.onEvent(AddMedicineEvent.OnDosageUnitChange(unit))
                                            isUnitDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = state.instructions,
                        onValueChange = { viewModel.onEvent(AddMedicineEvent.OnInstructionsChange(it)) },
                        label = { Text("Instructions (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.titleMedium
                    )

                    ExposedDropdownMenuBox(
                        expanded = isFrequencyDropdownExpanded,
                        onExpandedChange = { isFrequencyDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = state.frequency.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Frequency") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFrequencyDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = isFrequencyDropdownExpanded,
                            onDismissRequest = { isFrequencyDropdownExpanded = false }
                        ) {
                            Frequency.values().forEach { frequency ->
                                DropdownMenuItem(
                                    text = { Text(frequency.name) },
                                    onClick = {
                                        viewModel.onEvent(AddMedicineEvent.OnFrequencyChange(frequency))
                                        isFrequencyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (state.frequency == Frequency.WEEKLY) {
                        Column {
                            Text(
                                text = "Days",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (day in 1..7) {
                                    val isSelected = day in state.selectedDays
                                    val dayText = when (day) {
                                        1 -> "M"
                                        2 -> "T"
                                        3 -> "W"
                                        4 -> "T"
                                        5 -> "F"
                                        6 -> "S"
                                        7 -> "S"
                                        else -> ""
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clickable {
                                                viewModel.onEvent(AddMedicineEvent.OnDayToggle(day))
                                            },
                                        shape = MaterialTheme.shapes.small,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayText,
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Times",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(onClick = { showTimePicker = true }) {
                                Text("Add Time")
                            }
                        }

                        if (state.isTimesError) {
                            Text(
                                text = "At least one time is required",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        state.times.sorted().forEach { time ->
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
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Remove Time"
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Start Date")
                        TextButton(
                            onClick = {
                                isStartDate = true
                                showDatePicker = true
                            }
                        ) {
                            Text(state.startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("End Date (Optional)")
                        TextButton(
                            onClick = {
                                isStartDate = false
                                showDatePicker = true
                            }
                        ) {
                            Text(
                                state.endDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                                    ?: "Not Set"
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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
                    }
                }
            }

            Button(
                onClick = { viewModel.onEvent(AddMedicineEvent.OnSave) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Save")
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (isStartDate) {
                state.startDate.toLocalDate().toEpochDay() * 24 * 60 * 60 * 1000
            } else {
                state.endDate?.toLocalDate()?.toEpochDay()?.times(24 * 60 * 60 * 1000)
            }
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val day = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            
                            if (isStartDate) {
                                val newDateTime = LocalDateTime.of(
                                    day,
                                    state.startDate.toLocalTime()
                                )
                                viewModel.onEvent(AddMedicineEvent.OnStartDateChange(newDateTime))
                            } else {
                                val time = state.endDate?.toLocalTime() ?: LocalTime.of(23, 59)
                                val newDateTime = LocalDateTime.of(day, time)
                                viewModel.onEvent(AddMedicineEvent.OnEndDateChange(newDateTime))
                            }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = timePickerState.hour
                        val minute = timePickerState.minute
                        val now = LocalDateTime.now()
                        val timeDateTime = LocalDateTime.of(
                            now.year, now.month, now.dayOfMonth, hour, minute
                        )
                        viewModel.onEvent(AddMedicineEvent.OnTimeAdd(timeDateTime))
                        showTimePicker = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 