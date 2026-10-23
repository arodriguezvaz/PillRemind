package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.Schedule
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que define las operaciones relacionadas con los horarios de medicación
 */
interface ScheduleRepository {

    /**
     * Inserta un nuevo horario
     * @param schedule Horario a insertar
     */
    suspend fun insertSchedule(schedule: Schedule): Long

    /**
     * Actualiza un horario existente
     * @param schedule Horario a actualizar
     */
    suspend fun updateSchedule(schedule: Schedule)

    /**
     * Elimina un horario
     * @param schedule Horario a eliminar
     */
    suspend fun deleteSchedule(schedule: Schedule)

    /**
     * Elimina todos los horarios asociados a una medicación
     * @param medicationId ID de la medicación
     */
    suspend fun deleteByMedicationId(medicationId: Int)

    /**
     * Obtiene todos los horarios de una medicación
     * @param medicationId ID de la medicación
     */
    fun getSchedules(medicationId: Int): Flow<List<Schedule>>

    /**
     * Obtiene un horario por su ID, o null si ya no existe
     * @param scheduleId ID del horario
     */
    suspend fun getScheduleByIdOnce(scheduleId: Int): Schedule?

    /**
     * Obtiene todos los horarios de un usuario
     * @param userId ID del usuario propietario
     */
    suspend fun getAllByUser(userId: Int): List<Schedule>
}