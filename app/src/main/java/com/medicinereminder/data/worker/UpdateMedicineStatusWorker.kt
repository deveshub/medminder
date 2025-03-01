package com.medicinereminder.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.medicinereminder.domain.model.MedicineStatus
import com.medicinereminder.domain.repository.MedicineRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDateTime
import java.util.UUID

@HiltWorker
class UpdateMedicineStatusWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val medicineRepository: MedicineRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val medicineId = inputData.getString("medicineId")
            val statusString = inputData.getString("status")

            Log.d(TAG, "Starting work execution with medicineId: $medicineId, status: $statusString")

            if (medicineId == null) {
                Log.e(TAG, "Medicine ID is null")
                return Result.failure()
            }

            if (statusString == null) {
                Log.e(TAG, "Status is null")
                return Result.failure()
            }

            val status = try {
                MedicineStatus.valueOf(statusString)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Invalid status value: $statusString", e)
                return Result.failure()
            }

            Log.d(TAG, "Fetching medicine with ID: $medicineId")
            val medicine = medicineRepository.getMedicineById(UUID.fromString(medicineId))

            if (medicine == null) {
                Log.e(TAG, "Medicine not found with ID: $medicineId")
                return Result.failure()
            }

            Log.d(TAG, "Current medicine state - ID: ${medicine.id}, Status: ${medicine.status}")

            val updatedMedicine = medicine.copy(
                status = status,
                lastStatusUpdate = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            Log.d(TAG, "Updating medicine - New Status: ${updatedMedicine.status}, Last Update: ${updatedMedicine.lastStatusUpdate}")
            
            try {
                medicineRepository.updateMedicine(updatedMedicine)
                Log.d(TAG, "Successfully updated medicine in database")
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update medicine in database", e)
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in worker execution", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "UpdateMedicineStatusWorker"
    }
} 