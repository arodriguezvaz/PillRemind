package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.Medication
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define los métodos para interactuar con la base de datos de perfiles
 */
interface MedicationRepository {
    suspend fun insertMedication(medication: Medication)
    suspend fun deleteMedication(medication: Medication)
    suspend fun updateMedication(medication: Medication)
    fun getMedication(profileId: Int): Flow<List<Medication>>
}