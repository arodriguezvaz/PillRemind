package com.andres.pillremind.data.repository

import com.andres.pillremind.data.local.dao.IntakeDao
import com.andres.pillremind.data.local.dao.MedicationDao
import com.andres.pillremind.data.local.dao.ScheduleDao
import com.andres.pillremind.data.local.projections.IntakeWithMedication
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.mapper.toDomain
import com.andres.pillremind.data.mapper.toEntity
import com.andres.pillremind.domain.model.Intake
import com.andres.pillremind.domain.model.toWeekDay
import com.andres.pillremind.domain.repository.IntakeRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    override suspend fun createIntakes(desde: LocalDate, hasta: LocalDate): List<Intake> {
        val tomasAInsertar = mutableListOf<Intake>()

        var dia = desde
        while (!dia.isAfter(hasta)) {
            val horarios = scheduleDao.getSchedulesByDay(dia.dayOfWeek.toWeekDay()).first()

            for (horario in horarios) {
                val med = medicationDao.getMedicationById(horario.medicationId).first() ?: continue
                if (med.startDate > dia) continue
                if (med.endDate != null && med.endDate < dia) continue

                tomasAInsertar += Intake(
                    scheduleId = horario.id,
                    scheduledAt = LocalDateTime.of(dia, horario.time),
                )
            }
            dia = dia.plusDays(1)
        }

        intakeDao.insertIntakes(tomasAInsertar.map { it.toEntity() })
        return tomasAInsertar
    }

    override suspend fun insertIntake(intake: Intake): Long {
        return intakeDao.insertIntake(intake.toEntity())
    }


    override suspend fun deleteIntake(intake: Intake) {
        intakeDao.deleteIntake(intake.toEntity())
    }

    override suspend fun updateIntake(intake: Intake) {
        intakeDao.updateIntake(intake.toEntity())
    }

    override suspend fun getIntakeById(intakeId: Int): Intake {
        return intakeDao.getIntakeById(intakeId).toDomain()
    }

    override suspend fun getIntakesForToday(
        today: LocalDate,
        profileId: Int
    ): Flow<List<IntakeWithMedication>> {
        return intakeDao.getIntakesForToday(
            today.atStartOfDay(),
            today.atTime(LocalTime.MAX),
            profileId
        )
    }

    override suspend fun getLastGeneratedDate(): LocalDate {
        return sessionManager.lastGeneratedDate.first()
            ?: LocalDate.now().minusDays(30)
    }

    override suspend fun saveLastGeneratedDate(fecha: LocalDate) {
        sessionManager.saveLastGeneratedDate(fecha)
    }
}