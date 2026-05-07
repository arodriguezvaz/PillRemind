package com.andres.pillremind.data.repository

import com.andres.pillremind.data.local.dao.ScheduleDao
import com.andres.pillremind.data.local.entity.ScheduleEntity
import com.andres.pillremind.data.mapper.toDomain
import com.andres.pillremind.data.mapper.toEntity
import com.andres.pillremind.domain.model.Schedule
import com.andres.pillremind.domain.repository.ScheduleRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementación del repositorio de schedules
 * Gestiona el acceso a datos relacionados con los horarios de los medicamentos
 */
class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {

    override suspend fun insertSchedule(schedule: Schedule): Long {
        return scheduleDao.insertSchedule(schedule.toEntity())
    }

    override suspend fun updateSchedule(schedule: Schedule) {
        scheduleDao.updateSchedule(schedule.toEntity())
    }

    override suspend fun deleteSchedule(schedule: Schedule) {
        scheduleDao.deleteSchedule(schedule.toEntity())
    }

    override suspend fun deleteByMedicationId(medicationId: Int) {
        scheduleDao.deleteByMedicationId(medicationId)
    }


    override fun getSchedules(medicationId: Int): Flow<List<Schedule>> {
        return scheduleDao.getSchedules(medicationId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getScheduleById(scheduleId: Int): Schedule {
        return scheduleDao.getScheduleById(scheduleId).toDomain()
    }
}