package com.andres.pillremind.data.mapper


import com.andres.pillremind.data.local.entity.ScheduleEntity
import com.andres.pillremind.domain.model.Schedule

/**
 * Convierte un objeto ScheduleEntity en un objeto Schedule de dominio
 * @return Schedule
 * @receiver ScheduleEntity
 */
fun ScheduleEntity.toDomain(): Schedule = Schedule(
    id = id,
    medicationId = medicationId,
    weekDay = weekDay,
    time = time
)

/**
 * Convierte un objeto Schedule de dominio en un ScheduleEntity para la base de datos
 * @return ScheduleEntity
 * @receiver Schedule
 */
fun Schedule.toEntity(): ScheduleEntity = ScheduleEntity(
    id = id,
    medicationId = medicationId,
    weekDay = weekDay,
    time = time
)