package com.andres.pillremind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.andres.pillremind.data.local.entity.IntakeEntity
import com.andres.pillremind.data.local.projections.AdherenceRow
import com.andres.pillremind.data.local.projections.IntakeWithMedicationRow
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * DAO para gestionar las tomas (intakes) en la base de datos local.
 */
@Dao
interface IntakeDao {
    /**
     * Inserta una lista de tomas.
     *
     * Si ya existe una toma con el mismo `medicationId` y `scheduledAt`,
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
     * Marca una toma como tomada o pendiente sin reemplazar el resto de la fila.
     *
     * Se actualiza de forma dirigida para no sobrescribir los snapshots.
     *
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE intakes SET takenAt = :takenAt, updatedAt = :updatedAt WHERE id = :intakeId")
    suspend fun markTaken(
        intakeId: Int,
        takenAt: LocalDateTime?,
        updatedAt: LocalDateTime
    ): Int

    /**
     * Devuelve una toma por su ID, o null si ya no existe.
     */
    @Query("SELECT * FROM intakes WHERE id = :intakeId")
    suspend fun getIntakeById(intakeId: Int): IntakeEntity?

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
        i.medicationId,
        i.scheduledAt,
        i.scheduleId,
        i.takenAt,
        i.medicationNameSnapshot as medicationName,
        i.doseSnapshot as dose,
        i.unitSnapshot as unit,
        m.imageUrl,
        p.name as profileName
    FROM intakes i
    JOIN medications m ON m.id = i.medicationId
    JOIN profiles p ON p.id = m.profileId
    WHERE i.scheduledAt BETWEEN :startOfDay AND :endOfDay
    AND m.profileId = :profileId
    AND m.deletedAt IS NULL
    ORDER BY i.scheduledAt
    """
    )
    fun getIntakesForToday(
        startOfDay: LocalDateTime,
        endOfDay: LocalDateTime,
        profileId: Int
    ): Flow<List<IntakeWithMedicationRow>>

    /**
     * Devuelve los IDs de todas las tomas de un medicamento.
     *
     * Se usa para cancelar las notificaciones visibles antes de editar o
     * archivar el medicamento.
     */
    @Query("SELECT id FROM intakes WHERE medicationId = :medicationId")
    suspend fun getIntakeIdsByMedicationId(medicationId: Int): List<Int>

    /**
     * Calcula, por medicamento, las tomas registradas, omitidas y pendientes
     * dentro de un periodo.
     *
     * Se usa una consulta agregada para no cargar todo el historial en memoria.
     * Los medicamentos sin tomas en el periodo también aparecen con total cero.
     */
    @Query(
        """
    SELECT
        m.id AS medicationId,
        m.name AS medicationName,
        m.dose AS dose,
        m.unit AS unit,
        m.imageUrl AS imageUrl,
        m.deletedAt AS deletedAt,
        COUNT(i.id) AS total,
        COALESCE(SUM(CASE WHEN i.takenAt IS NOT NULL THEN 1 ELSE 0 END), 0) AS taken,
        COALESCE(SUM(
            CASE WHEN i.takenAt IS NULL AND i.scheduledAt < :now THEN 1 ELSE 0 END
        ), 0) AS missed
    FROM medications m
    LEFT JOIN intakes i
        ON i.medicationId = m.id
        AND i.scheduledAt >= :from
        AND i.scheduledAt <= :to
    WHERE m.profileId = :profileId
    GROUP BY m.id, m.name, m.dose, m.unit, m.imageUrl, m.deletedAt
    ORDER BY (m.deletedAt IS NOT NULL) ASC, m.name COLLATE NOCASE ASC
    """
    )
    fun getAdherence(
        profileId: Int,
        from: LocalDateTime,
        to: LocalDateTime,
        now: LocalDateTime
    ): Flow<List<AdherenceRow>>

    /**
     * Devuelve las tomas futuras de un medicamento desde una fecha.
     */
    @Query(
        """
    SELECT * FROM intakes 
    WHERE medicationId = :medicationId
    AND scheduledAt > :from
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
    AND medicationId IN (
        SELECT id FROM medications WHERE profileId = :profileId
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
    WHERE medicationId = :medicationId
    AND scheduledAt > :from
"""
    )
    suspend fun deleteFutureIntakesByMedicationId(medicationId: Int, from: LocalDateTime)
}
