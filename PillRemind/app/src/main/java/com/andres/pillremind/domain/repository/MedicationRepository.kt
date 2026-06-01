package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.Medication
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define las operaciones de acceso a datos relacionadas con medicamentos
 */
interface MedicationRepository {
    /**
     * Inserta un medicamento en la base de datos
     */
    suspend fun insertMedication(medication: Medication): Long

    /**
     * Elimina un medicamento de la base de datos.
     */
    suspend fun deleteMedication(medication: Medication)

    /**
     * Actualiza un medicamento en la base de datos
     */
    suspend fun updateMedication(medication: Medication)

    /**
     * Obtiene un medicamento de la base de datos
     */
    fun getMedicationById(medicationId: Int): Flow<Medication?>

    /**
     * Obtiene un medicamento de la base de datos
     */
    suspend fun getMedicationByIdOnce(medicationId: Int): Medication?

    /**
     * Obtiene todos los medicamentos de un perfil de la base de datos
     */
    fun getMedications(profileId: Int): Flow<List<Medication>>
    suspend fun getAllByUser(userId: Int): List<Medication>
    suspend fun deleteAllByUsuario(usuarioId: Int)
}