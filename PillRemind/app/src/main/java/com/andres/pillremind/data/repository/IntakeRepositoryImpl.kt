package com.andres.pillremind.data.repository

import com.andres.pillremind.data.local.dao.IntakeDao
import com.andres.pillremind.data.local.dao.MedicationDao
import com.andres.pillremind.data.local.dao.ScheduleDao
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.mapper.toDomain
import com.andres.pillremind.data.mapper.toEntity
import com.andres.pillremind.domain.model.Intake
import com.andres.pillremind.domain.model.IntakeWithMedication
import com.andres.pillremind.domain.model.toWeekDay
import com.andres.pillremind.domain.repository.IntakeRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class IntakeRepositoryImpl @Inject constructor(
    private val intakeDao: IntakeDao,
    private val scheduleDao: ScheduleDao,
    private val medicationDao: MedicationDao,
    private val sessionManager: SessionManager,
) :
    IntakeRepository {

    /**
     * Genera e inserta en la base de datos las tomas programadas dentro
     */
    override suspend fun createIntakes(desde: LocalDate, hasta: LocalDate): List<Intake> {
        val tomasAInsertar = mutableListOf<Intake>()

        var dia = desde
        while (!dia.isAfter(hasta)) {
            // Obtiene los horarios correspondientes al día de la semana actual
            val horarios = scheduleDao.getSchedulesByDay(dia.dayOfWeek.toWeekDay()).first()
            // Para cada horario, obtiene el medicamento asociado y genera la toma únicamente si el medicamento está activo en la fecha actual
            for (horario in horarios) {
                val med = medicationDao.getMedicationById(horario.medicationId).first() ?: continue
                if (med.deletedAt != null) continue
                if (med.startDate > dia) continue
                if (med.endDate != null && med.endDate < dia) continue

                tomasAInsertar += Intake(
                    medicationId = med.id,
                    scheduleId = horario.id,
                    scheduledAt = LocalDateTime.of(dia, horario.time),
                    medicationNameSnapshot = med.name,
                    doseSnapshot = med.dose,
                    unitSnapshot = med.unit
                )
            }
            dia = dia.plusDays(1)
        }
        // Inserta las tomas. Room devuelve -1 para las que ya existían por la
        // restricción UNIQUE, así que después se consultan sus IDs reales.
        if (tomasAInsertar.isEmpty()) return emptyList()

        val ids = intakeDao.insertIntakes(tomasAInsertar.map { it.toEntity() })
        val existentes = intakeDao.getIntakesBetween(
            desde.atStartOfDay(),
            hasta.atTime(LocalTime.MAX)
        ).associateBy { it.medicationId to it.scheduledAt }

        return tomasAInsertar.mapIndexed { index, intake ->
            val id = existentes[intake.medicationId to intake.scheduledAt]?.id
                ?: ids[index].toInt()
            intake.copy(id = id)
        }
    }

    override suspend fun markTaken(intakeId: Int, takenAt: LocalDateTime?) {
        intakeDao.markTaken(intakeId, takenAt, LocalDateTime.now())
    }

    override suspend fun getIntakeById(intakeId: Int): Intake? {
        return intakeDao.getIntakeById(intakeId)?.toDomain()
    }

    override fun getIntakesForToday(
        today: LocalDate,
        profileId: Int
    ): Flow<List<IntakeWithMedication>> {
        return intakeDao.getIntakesForToday(
            today.atStartOfDay(),
            today.atTime(LocalTime.MAX),
            profileId
        ).map { rows -> rows.map { it.toDomain() } }
    }

    override suspend fun getLastGeneratedDate(): LocalDate {
        return sessionManager.lastGeneratedDate.first()
            ?: LocalDate.now().minusDays(30)
    }

    override suspend fun saveLastGeneratedDate(fecha: LocalDate) {
        sessionManager.saveLastGeneratedDate(fecha)
    }

    override suspend fun getFutureIntakesByMedicationId(
        medicationId: Int,
        date: LocalDateTime
    ): List<Intake> {
        return intakeDao.getFutureIntakesByMedicationId(medicationId, date).map { it.toDomain() }
    }

    override suspend fun getFutureIntakesByProfileId(
        profileId: Int,
        date: LocalDateTime
    ): List<Intake> {
        return intakeDao.getFutureIntakesByProfileId(profileId, date).map { it.toDomain() }
    }

    override suspend fun deleteFutureIntakesByMedicationId(medicationId: Int, date: LocalDateTime) {
        intakeDao.deleteFutureIntakesByMedicationId(medicationId, date)
    }
}