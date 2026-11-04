package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.model.Schedule
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Interfaz que define las operaciones de acceso a datos relacionadas con medicamentos
 */
interface MedicationRepository {
    /**
     * Inserta un medicamento en la base de datos
     * @param medication Medicamento a insertar
     */
    suspend fun insertMedication(medication: Medication): Long

    /**
     * Inserta un medicamento y sus horarios en una sola transacción.
     */
    suspend fun insertMedicationWithSchedules(
        medication: Medication,
        schedules: List<Schedule>
    ): Long

    /**
     * Archiva un medicamento sin borrar su historial de tomas.
     *
     * Los horarios se conservan para poder recuperar la pauta si el usuario
     * restaura el medicamento. Mientras esté archivado no se generan tomas.
     */
    suspend fun softDeleteMedication(
        medicationId: Int,
        deletedAt: LocalDateTime,
        deleteFutureFrom: LocalDateTime
    )

    /**
     * Recupera un medicamento archivado.
     *
     * @param medicationId ID del medicamento.
     * @param restoredAt Momento de la restauración.
     */
    suspend fun restoreMedication(medicationId: Int, restoredAt: LocalDateTime)

    /**
     * Borra definitivamente un medicamento y todo su historial.
     *
     * @param medicationId ID del medicamento.
     */
    suspend fun deleteMedicationPermanently(medicationId: Int)

    /**
     * Actualiza un medicamento en la base de datos
     * @param medication Medicamento a actualizar
     */
    suspend fun updateMedication(medication: Medication)

    /**
     * Actualiza un medicamento y reemplaza sus horarios en una sola transacción.
     */
    suspend fun updateMedicationWithSchedules(
        medication: Medication,
        schedules: List<Schedule>,
        deleteFutureFrom: LocalDateTime
    )

    /**
     * Obtiene un medicamento de la base de datos
     * @param medicationId ID del medicamento
     */
    fun getMedicationById(medicationId: Int): Flow<Medication?>

    /**
     * Obtiene un medicamento de la base de datos
     * @param medicationId ID del medicamento
     */
    suspend fun getMedicationByIdOnce(medicationId: Int): Medication?

    /**
     * Obtiene todos los medicamentos de un perfil de la base de datos
     * @param profileId ID del perfil
     */
    fun getMedications(profileId: Int): Flow<List<Medication>>

    /**
     * Observa los medicamentos archivados de un perfil.
     */
    fun getArchivedMedications(profileId: Int): Flow<List<Medication>>

    /**
     * Obtiene una sola vez todos los medicamentos de un perfil, incluidos los
     * archivados. Se usa para limpiar imágenes antes de borrar el perfil.
     */
    suspend fun getMedicationsOnce(profileId: Int): List<Medication>
}