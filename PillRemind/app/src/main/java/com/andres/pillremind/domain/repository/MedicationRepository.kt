package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.model.Profile
import kotlinx.coroutines.flow.Flow

/**
 *
 */
interface MedicationRepository {
    suspend fun insertMedication(medication: Medication)
    suspend fun deleteMedication(medication: Medication)
    suspend fun updateMedication(medication: Medication)
    fun getMedication(profileId: Int): Flow<List<Medication>>
}