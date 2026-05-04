package com.andres.pillremind.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.andres.pillremind.data.local.entity.ScheduleEntity
import com.andres.pillremind.domain.model.Schedule
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
    suspend fun insertSchedule(scheduleEntity: ScheduleEntity)

    /**
     * Actualiza un schedule en la base de datos
     * @param schedule Schedule a actualizar
     */
    @Update
    suspend fun updateSchedule(scheduleEntity: ScheduleEntity)

    /**
     * Elimina un schedule de la base de datos
     */
    @Delete
    suspend fun deleteSchedule(scheduleEntity: ScheduleEntity)

    /**
     * Obtiene todos los schedules de un medication de la base de datos
     */
    @Query("SELECT * FROM schedule WHERE medicationId = :medicationId")
    fun getSchedules(medicationId: Int): Flow<List<ScheduleEntity>>
}
