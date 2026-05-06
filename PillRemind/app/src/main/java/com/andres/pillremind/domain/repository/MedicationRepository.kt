package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.Medication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interfaz que define los métodos para interactuar con la base de datos de perfiles
 */
interface MedicationRepository {
    /**
     * Inserta un medicamento en la base de datos
     */
    suspend fun insertMedication(medication: Medication): Long

    /**
     * Actualiza un medicamento en la base de datos
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
     * Obtiene todos los medicamentos de un perfil de la base de datos
     */
    fun getMedications(profileId: Int): Flow<List<Medication>>
}