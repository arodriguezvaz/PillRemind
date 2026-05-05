package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.Schedule
import kotlinx.coroutines.flow.Flow

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

    /**
     * Elimina todos los horarios asociados a una medicación
     */
    suspend fun deleteByMedicationId(medicationId: Int)

    /**
     * Obtiene todos los horarios de una medicación
     */
    fun getSchedules(medicationId: Int): Flow<List<Schedule>>
}