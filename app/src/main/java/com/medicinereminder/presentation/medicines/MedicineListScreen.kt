package com.medicinereminder.presentation.medicines

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medicinereminder.domain.model.Medicine
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
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddMedicine) {
                Icon(Icons.Default.Add, contentDescription = "Add Medicine")
            }
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
            }

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            if (state.medicines.isEmpty() && !state.isLoading) {
                Text(
                    text = "No medicines added yet",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.medicines) { medicine ->
                    MedicineItem(
                        medicine = medicine,
                        onDelete = { viewModel.deleteMedicine(medicine) },
                        onClick = { onNavigateToMedicineDetails(medicine.id.toString()) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineItem(
    medicine: Medicine,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = medicine.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${medicine.dosage.amount} ${medicine.dosage.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Next: ${formatDateTime(medicine.schedule.times.firstOrNull())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Medicine",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatDateTime(dateTime: java.time.LocalDateTime?): String {
    if (dateTime == null) return "Not scheduled"
    val now = java.time.LocalDateTime.now()
    if (dateTime.isBefore(now)) {
        val nextDateTime = dateTime.toLocalTime().atDate(now.toLocalDate())
        if (nextDateTime.isBefore(now)) {
            return nextDateTime.plusDays(1).format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))
        }
        return nextDateTime.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))
    }
    return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))
} 