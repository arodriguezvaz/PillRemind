package com.andres.pillremind.data.repository

import androidx.room.withTransaction
import com.andres.pillremind.data.local.dao.IntakeDao
import com.andres.pillremind.data.local.dao.MedicationDao
import com.andres.pillremind.data.local.dao.ScheduleDao
import com.andres.pillremind.data.local.database.AppDatabase
import com.andres.pillremind.data.mapper.toDomain
import com.andres.pillremind.data.mapper.toEntity
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.model.Schedule
import com.andres.pillremind.domain.repository.MedicationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

/**
 * Implementación del repositorio de medicamentos.
 * Gestiona el acceso a datos relacionados con medicamentos.
 */
class MedicationRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val medicationDao: MedicationDao,
    private val scheduleDao: ScheduleDao,
    private val intakeDao: IntakeDao
) : MedicationRepository {

    override suspend fun insertMedication(medication: Medication): Long {
        return medicationDao.insertMedication(medication.toEntity())
    }

    override suspend fun insertMedicationWithSchedules(
        medication: Medication,
        schedules: List<Schedule>
    ): Long = database.withTransaction {
        val medicationId = medicationDao.insertMedication(medication.toEntity())
        schedules.forEach { schedule ->
            scheduleDao.insertSchedule(
                schedule.copy(medicationId = medicationId.toInt()).toEntity()
            )
        }
        medicationId
    }

    override suspend fun deleteMedication(medication: Medication) {
        medicationDao.deleteMedication(medication.toEntity())
    }

    override suspend fun softDeleteMedication(
        medicationId: Int,
        deletedAt: LocalDateTime,
        deleteFutureFrom: LocalDateTime
    ) {
        database.withTransaction {
            medicationDao.markDeleted(medicationId, deletedAt, deletedAt)
            intakeDao.deleteFutureIntakesByMedicationId(medicationId, deleteFutureFrom)
            scheduleDao.deleteByMedicationId(medicationId)
        }
    }

    override suspend fun updateMedication(medication: Medication) {
        medicationDao.updateMedication(medication.toEntity())
    }

    override suspend fun updateMedicationWithSchedules(
        medication: Medication,
        schedules: List<Schedule>,
        deleteFutureFrom: LocalDateTime
    ) {
        database.withTransaction {
            medicationDao.updateMedication(medication.toEntity())
            intakeDao.deleteFutureIntakesByMedicationId(medication.id, deleteFutureFrom)
            scheduleDao.deleteByMedicationId(medication.id)
            schedules.forEach { schedule ->
                scheduleDao.insertSchedule(schedule.copy(medicationId = medication.id).toEntity())
            }
        }
    }

    override fun getMedicationById(medicationId: Int): Flow<Medication?> {
        return medicationDao.getMedicationById(medicationId).map { it?.toDomain() }
    }

    override suspend fun getMedicationByIdOnce(medicationId: Int): Medication? {
        return medicationDao.getMedicationByIdOnce(medicationId)?.toDomain()
    }

    override fun getMedications(profileId: Int): Flow<List<Medication>> {
        return medicationDao.getMedications(profileId).map { list -> list.map { it.toDomain() } }
    }
}
