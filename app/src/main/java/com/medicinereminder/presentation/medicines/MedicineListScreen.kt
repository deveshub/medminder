package com.medicinereminder.presentation.medicines

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medicinereminder.domain.model.Medicine
import com.medicinereminder.domain.model.MedicineStatus
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineListScreen(
    onNavigateToAddMedicine: () -> Unit,
    onNavigateToMedicineDetails: (String) -> Unit,
    viewModel: MedicineListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicine Reminder") },
                actions = {
                    IconButton(onClick = onNavigateToAddMedicine) {
                        Icon(Icons.Default.Add, contentDescription = "Add Medicine")
                    }
                }
            )
        }
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
            } else if (state.medicines.isEmpty()) {
                Text(
                    text = "No medicines added yet",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.medicines) { medicine ->
                        MedicineCard(
                            medicine = medicine,
                            onMedicineClick = { onNavigateToMedicineDetails(medicine.id.toString()) }
                        )
                    }
                }
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineCard(
    medicine: Medicine,
    onMedicineClick: () -> Unit
) {
    Card(
        onClick = onMedicineClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = medicine.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${medicine.dosage.amount} ${medicine.dosage.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    medicine.schedule.times.firstOrNull()?.let { time ->
                        Text(
                            text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (medicine.schedule.times.size > 1) {
                        Text(
                            text = "+${medicine.schedule.times.size - 1} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Status Icon
            medicine.lastStatusUpdate?.let { lastUpdate ->
                val today = java.time.LocalDate.now()
                val statusDate = lastUpdate.toLocalDate()
                
                if (statusDate == today) {
                    when (medicine.status) {
                        MedicineStatus.TAKEN -> Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Taken",
                            tint = Color(0xFF4CAF50)
                        )
                        MedicineStatus.SNOOZED -> Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Snoozed",
                            tint = Color(0xFFFFA000)
                        )
                        MedicineStatus.SKIPPED -> Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Skipped",
                            tint = Color(0xFFF44336)
                        )
                        MedicineStatus.PENDING -> Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Pending",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
} 