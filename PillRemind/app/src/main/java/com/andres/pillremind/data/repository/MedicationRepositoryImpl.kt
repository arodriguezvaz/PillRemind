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
import com.andres.pillremind.domain.model.requireValidForSaving
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
        medication.requireValidForSaving()
        return medicationDao.insertMedication(medication.toEntity())
    }

    override suspend fun insertMedicationWithSchedules(
        medication: Medication,
        schedules: List<Schedule>
    ): Long = database.withTransaction {
        medication.requireValidForSaving()
        val medicationId = medicationDao.insertMedication(medication.toEntity())
        schedules.forEach { schedule ->
            scheduleDao.insertSchedule(
                schedule.copy(medicationId = medicationId.toInt()).toEntity()
            )
        }
        medicationId
    }

    override suspend fun softDeleteMedication(
        medicationId: Int,
        deletedAt: LocalDateTime,
        deleteFutureFrom: LocalDateTime
    ) {
        database.withTransaction {
            medicationDao.markDeleted(medicationId, deletedAt, deletedAt)
            intakeDao.deleteFutureIntakesByMedicationId(medicationId, deleteFutureFrom)
            // Los horarios se conservan para que una restauración recupere la pauta.
        }
    }

    override suspend fun restoreMedication(medicationId: Int, restoredAt: LocalDateTime) {
        medicationDao.restoreMedication(medicationId, restoredAt)
    }

    override suspend fun deleteMedicationPermanently(medicationId: Int) {
        medicationDao.deleteMedicationById(medicationId)
    }

    override suspend fun updateMedication(medication: Medication) {
        medication.requireValidForSaving()
        medicationDao.updateMedication(medication.toEntity())
    }

    override suspend fun updateMedicationWithSchedules(
        medication: Medication,
        schedules: List<Schedule>,
        deleteFutureFrom: LocalDateTime
    ) {
        medication.requireValidForSaving()
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

    override fun getArchivedMedications(profileId: Int): Flow<List<Medication>> {
        return medicationDao.getArchivedMedications(profileId)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getMedicationsOnce(profileId: Int): List<Medication> {
        return medicationDao.getMedicationsOnce(profileId).map { it.toDomain() }
    }
}
