package com.andres.pillremind.data.repository

import com.andres.pillremind.data.local.dao.ScheduleDao
import com.andres.pillremind.data.local.entity.ScheduleEntity
import com.andres.pillremind.data.mapper.toEntity
import com.andres.pillremind.domain.model.Schedule
import com.andres.pillremind.domain.repository.ScheduleRepository
import jakarta.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {

    override suspend fun insertSchedule(schedule: Schedule) {
        scheduleDao.insertSchedule(schedule.toEntity())
    }

    override suspend fun updateSchedule(schedule: Schedule) {
        scheduleDao.updateSchedule(schedule.toEntity())
    }

    override suspend fun deleteSchedule(schedule: Schedule) {
        scheduleDao.deleteSchedule(schedule.toEntity())
    }
}