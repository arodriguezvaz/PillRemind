package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.Intake
import com.andres.pillremind.domain.model.IntakeWithMedication
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
     * Obtiene todas las tomas de un perfil para hoy
     */
    fun getIntakesForToday(
        today: LocalDate,
        profileId: Int
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
     * Elimina las tomas futuras de una medicación
     * @param medicationId ID de la medicación
     * @param date a partir de la que eliiminar las tomas
     */
    suspend fun deleteFutureIntakesByMedicationId(medicationId: Int, date: LocalDateTime)
}

