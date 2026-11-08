package com.andres.pillremind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.andres.pillremind.data.local.entity.IntakeEntity
import com.andres.pillremind.data.local.projections.IntakeWithMedicationRow
import com.andres.pillremind.data.local.projections.StatisticsIntakeRow
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
     * Observa las tomas programadas de un día para un perfil, con datos del medicamento.
     *
     * @param startOfDay Inicio del día.
     * @param endOfDay Fin del día.
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
    fun observeIntakesForDay(
        startOfDay: LocalDateTime,
        endOfDay: LocalDateTime,
        profileId: Int
    ): Flow<List<IntakeWithMedicationRow>>

    /**
     * Observa las tomas de un perfil dentro de un rango para calcular
     * estadísticas e historial.
     *
     * Incluye los medicamentos archivados que conservan su historial. No se
     * filtra por `deletedAt` para no perder las tomas anteriores al archivo.
     *
     * @param profileId ID del perfil propietario.
     * @param from Inicio del rango.
     * @param to Fin del rango.
     * @return Flow con las tomas ordenadas por fecha programada.
     */
    @Query(
        """
    SELECT
        i.id AS intakeId,
        i.medicationId,
        i.scheduledAt,
        i.takenAt,
        i.medicationNameSnapshot AS medicationName,
        i.doseSnapshot AS dose,
        i.unitSnapshot AS unit,
        m.imageUrl,
        m.deletedAt
    FROM intakes i
    JOIN medications m ON m.id = i.medicationId
    WHERE m.profileId = :profileId
    AND i.scheduledAt BETWEEN :from AND :to
    ORDER BY i.scheduledAt
    """
    )
    fun observeStatisticsIntakes(
        profileId: Int,
        from: LocalDateTime,
        to: LocalDateTime
    ): Flow<List<StatisticsIntakeRow>>

    /**
     * Observa las tomas de un perfil para la pantalla de historial.
     *
     * A diferencia de [observeStatisticsIntakes], ordena de más reciente a más
     * antigua y permite un rango abierto con `from` o `to` nulos. Incluye los
     * medicamentos archivados que conservan su historial.
     *
     * @param profileId ID del perfil propietario.
     * @param from Inicio del rango, o null para no limitar por abajo.
     * @param to Fin del rango, o null para no limitar por arriba.
     * @return Flow con las tomas ordenadas de más reciente a más antigua.
     */
    @Query(
        """
    SELECT
        i.id AS intakeId,
        i.medicationId,
        i.scheduledAt,
        i.takenAt,
        i.medicationNameSnapshot AS medicationName,
        i.doseSnapshot AS dose,
        i.unitSnapshot AS unit,
        m.imageUrl,
        m.deletedAt
    FROM intakes i
    JOIN medications m ON m.id = i.medicationId
    WHERE m.profileId = :profileId
    AND (:from IS NULL OR i.scheduledAt >= :from)
    AND (:to IS NULL OR i.scheduledAt <= :to)
    ORDER BY i.scheduledAt DESC
    """
    )
    fun observeHistoryIntakes(
        profileId: Int,
        from: LocalDateTime?,
        to: LocalDateTime?
    ): Flow<List<StatisticsIntakeRow>>

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
     * Devuelve los IDs de todas las tomas de un medicamento.
     *
     * Se usa para cancelar las notificaciones asociadas antes de archivar,
     * editar o borrar el medicamento.
     *
     * @param medicationId ID del medicamento.
     */
    @Query("SELECT id FROM intakes WHERE medicationId = :medicationId")
    suspend fun getIntakeIdsByMedicationId(medicationId: Int): List<Int>

    /**
     * Devuelve los IDs de todas las tomas de un perfil.
     *
     * Se usa para cancelar sus notificaciones antes de borrar el perfil y sus
     * medicamentos en cascada.
     *
     * @param profileId ID del perfil propietario.
     */
    @Query(
        """
    SELECT i.id FROM intakes i
    JOIN medications m ON m.id = i.medicationId
    WHERE m.profileId = :profileId
"""
    )
    suspend fun getIntakeIdsByProfileId(profileId: Int): List<Int>

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
