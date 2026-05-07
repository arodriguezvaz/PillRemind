package com.andres.pillremind.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.andres.pillremind.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define los métodos para interactuar con la base de datos
 */
@Dao
interface ScheduleDao {
    /**
     * Inserta un schedule en la base de datos
     */
    @Insert
    suspend fun insertSchedule(scheduleEntity: ScheduleEntity): Long

    /**
     * Actualiza un schedule en la base de datos
     * @param scheduleEntity ScheduleEntity a actualizar
     */
    @Update
    suspend fun updateSchedule(scheduleEntity: ScheduleEntity)

    /**
     * Elimina un schedule de la base de datos
     */
    @Delete
    suspend fun deleteSchedule(scheduleEntity: ScheduleEntity)

    /**
     * Elimina todos los schedules de un medication de la base de datos
     */
    @Query("DELETE FROM schedules WHERE medicationId = :medicationId")
    suspend fun deleteByMedicationId(medicationId: Int)

    /**
     * Obtiene todos los schedules de un medication de la base de datos
     */
    @Query("SELECT * FROM schedules WHERE medicationId = :medicationId")
    fun getSchedules(medicationId: Int): Flow<List<ScheduleEntity>>

    /**
     * Obtiene un schedule a a partir de su id
     */
    @Query("SELECT * FROM schedules WHERE id = :scheduleId")
    suspend fun getScheduleById(scheduleId: Int): ScheduleEntity
}
