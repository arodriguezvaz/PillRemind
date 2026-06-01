package com.andres.pillremind.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.andres.pillremind.data.local.entity.IntakeEntity
import com.andres.pillremind.data.local.projections.IntakeWithMedication
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Interfaz que define los métodos para interactuar con la base de datos
 */
@Dao
interface IntakeDao {
    /**
     * Inserta un intake en la base de datos
     */
    @Insert
    suspend fun insertIntake(intake: IntakeEntity): Long

    /**
     * Inserta una lista de tomas en la base de datos.
     *
     * Si ya existe una toma con el mismo `scheduleId` y `scheduledAt`,
     * la inserción de esa toma se ignora automáticamente debido a la
     * restricción UNIQUE definida en la entidad.
     *
     *
     * @param intakes Lista de tomas a insertar.
     *
     * @return Lista con los IDs generados para las tomas insertadas.
     * Las tomas ignoradas por conflicto devuelven `-1`.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIntakes(intakes: List<IntakeEntity>): List<Long>

    /**
     * Elimina un intake de la base de datos
     */
    @Delete
    suspend fun deleteIntake(intake: IntakeEntity)

    /**
     * Actualiza un intake en la base de datos
     */
    @Update
    suspend fun updateIntake(intake: IntakeEntity)

    /**
     * Obtiene un intake por su ID
     */
    @Query("SELECT * FROM intakes WHERE id = :intakeId")
    fun getIntakeById(intakeId: Int): IntakeEntity

    /**
     * Observa las tomas programadas para el día actual de un perfil concreto,
     * incluyendo información del medicamento asociado.
     *
     * La consulta combina datos de IntakeEntity,ScheduleEntity, MedicationEntity
     * y devuelve una proyección optimizada para mostrar las tomas en la pantalla principal.
     *
     * @param today Fecha para filtrar las tomas programadas.
     * @param profileId ID del perfil propietario de los medicamentos.
     *
     * @return Flow reactivo con la lista de tomas del día junto a la información
     * del medicamento asociado, ordenadas por fecha programada.
     */
    @Query(
        """
    SELECT 
        i.id as intakeId,
        i.scheduledAt,
        i.scheduleId,
        i.supabaseId,
        i.takenAt,
        m.name as medicationName,
        m.dose,
        m.unit,
        m.imageUrl,
        p.name as profileName
    FROM intakes i
    JOIN schedules s ON s.id = i.scheduleId
    JOIN medications m ON m.id = s.medicationId
    JOIN profiles p ON p.id = m.profileId
    WHERE i.scheduledAt BETWEEN :startOfDay AND :endOfDay
    AND m.profileId = :profileId
    ORDER BY i.scheduledAt
    """
    )
    fun getIntakesForToday(
        startOfDay: LocalDateTime,
        endOfDay: LocalDateTime,
        profileId: Int
    ): Flow<List<IntakeWithMedication>>

    @Query("SELECT * FROM intakes WHERE scheduleId IN (SELECT id FROM schedules WHERE medicationId IN (SELECT id FROM medications WHERE profileId IN (SELECT id FROM profiles WHERE usuarioId = :usuarioId)))")
    suspend fun getAllByUser(usuarioId: Int): List<IntakeEntity>

}