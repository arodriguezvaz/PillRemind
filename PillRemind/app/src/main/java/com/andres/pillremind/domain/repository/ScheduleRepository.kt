package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.Schedule

/**
 * Repositorio que define las operaciones relacionadas con los horarios de medicación
 */
interface ScheduleRepository {

    /**
     * Inserta un nuevo horario
     */
    suspend fun insertSchedule(schedule: Schedule)

    /**
     * Actualiza un horario existente
     */
    suspend fun updateSchedule(schedule: Schedule)

    /**
     * Elimina un horario
     */
    suspend fun deleteSchedule(schedule: Schedule)
}