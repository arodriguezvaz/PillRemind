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
 * DAO para gestionar las tomas (intakes) en la base de datos local.
 */
@Dao
interface IntakeDao {
    /**
     * Inserta una toma y devuelve el ID generado.
     */
    @Insert
    suspend fun insertIntake(intake: IntakeEntity): Long

    /**
     * Inserta una lista de tomas.
     *
     * Si ya existe una toma con el mismo `scheduleId` y `scheduledAt`,
     * la insercion de esa toma se ignora por la restriccion UNIQUE.
     *
     * @param intakes Lista de tomas a insertar.
     * @return Lista con los IDs generados; las tomas ignoradas devuelven `-1`.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIntakes(intakes: List<IntakeEntity>): List<Long>

    /**
     * Devuelve las tomas cuya fecha programada está dentro del rango indicado.
     *
     * Se usa para recuperar los IDs reales de tomas que ya existían y que Room
     * no devuelve al insertar con [OnConflictStrategy.IGNORE].
     */
    @Query("SELECT * FROM intakes WHERE scheduledAt BETWEEN :start AND :end")
    suspend fun getIntakesBetween(start: LocalDateTime, end: LocalDateTime): List<IntakeEntity>

    /**
     * Elimina una toma existente.
     */
    @Delete
    suspend fun deleteIntake(intake: IntakeEntity)

    /**
     * Actualiza una toma existente.
     */
    @Update
    suspend fun updateIntake(intake: IntakeEntity)

    /**
     * Devuelve una toma por su ID, o null si ya no existe.
     */
    @Query("SELECT * FROM intakes WHERE id = :intakeId")
    fun getIntakeById(intakeId: Int): IntakeEntity?

    /**
     * Observa las tomas programadas del dia para un perfil, con datos del medicamento.
     *
     * @param startOfDay Inicio del dia.
     * @param endOfDay Fin del dia.
     * @param profileId ID del perfil propietario.
     * @return Flow con la lista ordenada por fecha programada.
     */
    @Query(
        """
    SELECT 
        i.id as intakeId,
        i.scheduledAt,
        i.scheduleId,
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

    /**
     * Devuelve todas las tomas pertenecientes a un usuario.
     */
    @Query("SELECT * FROM intakes WHERE scheduleId IN (SELECT id FROM schedules WHERE medicationId IN (SELECT id FROM medications WHERE profileId IN (SELECT id FROM profiles WHERE usuarioId = :usuarioId)))")
    suspend fun getAllByUser(usuarioId: Int): List<IntakeEntity>

    /**
     * Devuelve las tomas futuras de un medicamento desde una fecha.
     */
    @Query(
        """
    SELECT * FROM intakes 
    WHERE scheduledAt > :from
    AND scheduleId IN (
        SELECT id FROM schedules WHERE medicationId = :medicationId
    )
"""
    )
    suspend fun getFutureIntakesByMedicationId(
        medicationId: Int,
        from: LocalDateTime
    ): List<IntakeEntity>

    /**
     * Devuelve las tomas futuras de todos los medicamentos de un perfil.
     */
    @Query(
        """
    SELECT * FROM intakes
    WHERE scheduledAt > :from
    AND scheduleId IN (
        SELECT id FROM schedules WHERE medicationId IN (
            SELECT id FROM medications WHERE profileId = :profileId
        )
    )
"""
    )
    suspend fun getFutureIntakesByProfileId(
        profileId: Int,
        from: LocalDateTime
    ): List<IntakeEntity>

    /**
     * Elimina las tomas futuras de un medicamento desde una fecha.
     */
    @Query(
        """
    DELETE FROM intakes 
    WHERE scheduledAt > :from
    AND scheduleId IN (
        SELECT id FROM schedules WHERE medicationId = :medicationId
    )
"""
    )
    suspend fun deleteFutureIntakesByMedicationId(medicationId: Int, from: LocalDateTime)
}