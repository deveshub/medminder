package com.medicinereminder.domain.usecase

import com.medicinereminder.domain.model.Medicine
import com.medicinereminder.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject

class GetUpcomingRemindersUseCase @Inject constructor(
    private val repository: MedicineRepository
) {
    operator fun invoke(
        from: LocalDateTime = LocalDateTime.now(),
        hoursAhead: Long = 24
    ): Flow<List<Medicine>> {
        val to = from.plusHours(hoursAhead)
        return repository.getMedicinesDueForReminder(from, to)
    }
} 