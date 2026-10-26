package com.andres.pillremind.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.andres.pillremind.data.local.entity.ScheduleEntity
import com.andres.pillremind.domain.model.WeekDay
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define los métodos para interactuar con la base de datos.
 */
@Dao
interface ScheduleDao {
    /**
     * Inserta un schedule y devuelve el ID generado.
     *
     * @param scheduleEntity Entidad del schedule a insertar.
     * @return ID generado por la base de datos.
     */
    @Insert
    suspend fun insertSchedule(scheduleEntity: ScheduleEntity): Long

    /**
     * Actualiza un schedule existente.
     *
     * @param scheduleEntity Entidad del schedule con los cambios.
     */
    @Update
    suspend fun updateSchedule(scheduleEntity: ScheduleEntity)

    /**
     * Elimina un schedule existente.
     *
     * @param scheduleEntity Entidad del schedule a eliminar.
     */
    @Delete
    suspend fun deleteSchedule(scheduleEntity: ScheduleEntity)

    /**
     * Elimina todos los schedules de un medicamento.
     *
     * @param medicationId ID del medicamento propietario.
     */
    @Query("DELETE FROM schedules WHERE medicationId = :medicationId")
    suspend fun deleteByMedicationId(medicationId: Int)

    /**
     * Observa todos los schedules de un medicamento.
     *
     * @param medicationId ID del medicamento propietario.
     * @return Flow con la lista de schedules del medicamento.
     */
    @Query("SELECT * FROM schedules WHERE medicationId = :medicationId")
    fun getSchedules(medicationId: Int): Flow<List<ScheduleEntity>>

    /**
     * Devuelve un schedule por su ID, o null si ya no existe.
     *
     * @param scheduleId ID del schedule.
     * @return Schedule encontrado o null.
     */
    @Query("SELECT * FROM schedules WHERE id = :scheduleId")
    suspend fun getScheduleById(scheduleId: Int): ScheduleEntity?

    /**
     * Observa los schedules de un dia concreto de la semana.
     *
     * @param day Dia de la semana a filtrar.
     * @return Flow con la lista de schedules del dia.
     */
    @Query("SELECT * FROM schedules WHERE WeekDay = :day")
    fun getSchedulesByDay(day: WeekDay): Flow<List<ScheduleEntity>>
}
