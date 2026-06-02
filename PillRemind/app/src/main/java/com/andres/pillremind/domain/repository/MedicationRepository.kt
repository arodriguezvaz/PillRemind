package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.Medication
import kotlinx.coroutines.flow.Flow

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
     * Elimina un medicamento de la base de datos.
     * @param medication Medicamento a eliminar
     */
    suspend fun deleteMedication(medication: Medication)

    /**
     * Actualiza un medicamento en la base de datos
     * @param medication Medicamento a actualizar
     */
    suspend fun updateMedication(medication: Medication)

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
     * Obtiene todos los medicamentos de un usuario
     * @param userId ID del usuario propietario
     */
    suspend fun getAllByUser(userId: Int): List<Medication>

    /**
     * Elimina todos los medicamentos de un usuario
     * @param usuarioId ID del usuario propietario
     */
    suspend fun deleteAllByUsuario(usuarioId: Int)
}