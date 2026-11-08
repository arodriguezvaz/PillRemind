package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.Intake
import com.andres.pillremind.domain.model.IntakeWithMedication
import com.andres.pillremind.domain.model.StatisticsIntake
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Interfaz que define las operaciones de acceso a datos relacionadas con tomas
 */
interface IntakeRepository {
    /**
     * Marca una toma como tomada o pendiente sin modificar sus snapshots.
     */
    suspend fun markTaken(intakeId: Int, takenAt: LocalDateTime?)

    /**
     * Obtiene una toma de medicación de la base de datos
     */
    suspend fun getIntakeById(intakeId: Int): Intake?

    /**
     * Observa todas las tomas de un perfil para el día indicado.
     *
     * @param profileId ID del perfil propietario.
     * @param date Día del que se obtienen las tomas.
     */
    fun observeIntakesForDay(
        profileId: Int,
        date: LocalDate
    ): Flow<List<IntakeWithMedication>>

    /**
     * Crea las tomas desde una fecha a otra
     * @param desde Fecha de inicio
     * @param hasta Fecha de fin
     */
    suspend fun createIntakes(desde: LocalDate, hasta: LocalDate): List<Intake>

    /**
     * Obtiene la fecha de la última generación de tomas.
     * Si no hay ninguna guardada, devuelve hoy.
     */
    suspend fun getLastGeneratedDate(): LocalDate

    /**
     * Guarda la fecha de la última generación de tomas
     * @param fecha Fecha de la última generación de tomas
     */
    suspend fun saveLastGeneratedDate(fecha: LocalDate)

    /**
     * Obtiene las tomas futuras de una medicación
     * @param medicationId ID de la medicación
     * @param date a partir de la que obtener las tomas
     */
    suspend fun getFutureIntakesByMedicationId(medicationId: Int, date: LocalDateTime): List<Intake>

    /**
     * Obtiene las tomas futuras de todos los medicamentos de un perfil
     * @param profileId ID del perfil
     * @param date a partir de la que obtener las tomas
     */
    suspend fun getFutureIntakesByProfileId(profileId: Int, date: LocalDateTime): List<Intake>

    /**
     * Obtiene los IDs de todas las tomas de un medicamento.
     *
     * Se usa para cancelar sus notificaciones al archivar, editar o borrar.
     *
     * @param medicationId ID de la medicación.
     */
    suspend fun getIntakeIdsByMedicationId(medicationId: Int): List<Int>

    /**
     * Obtiene los IDs de todas las tomas de un perfil.
     *
     * Se usa para cancelar sus notificaciones antes de borrar el perfil.
     *
     * @param profileId ID del perfil.
     */
    suspend fun getIntakeIdsByProfileId(profileId: Int): List<Int>

    /**
     * Observa las tomas de un perfil dentro de un rango para calcular
     * estadísticas e historial.
     *
     * Incluye los medicamentos archivados que conservan su historial.
     *
     * @param profileId ID del perfil propietario.
     * @param from Inicio del rango.
     * @param to Fin del rango.
     */
    fun observeStatisticsIntakes(
        profileId: Int,
        from: LocalDateTime,
        to: LocalDateTime
    ): Flow<List<StatisticsIntake>>

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
     */
    fun observeHistoryIntakes(
        profileId: Int,
        from: LocalDateTime?,
        to: LocalDateTime?
    ): Flow<List<StatisticsIntake>>

    /**
     * Elimina las tomas futuras de una medicación
     * @param medicationId ID de la medicación
     * @param date a partir de la que eliiminar las tomas
     */
    suspend fun deleteFutureIntakesByMedicationId(medicationId: Int, date: LocalDateTime)
}

