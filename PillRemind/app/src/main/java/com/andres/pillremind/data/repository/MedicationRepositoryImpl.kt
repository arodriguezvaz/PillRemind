package com.andres.pillremind.data.repository

import com.andres.pillremind.data.local.dao.MedicationDao
import com.andres.pillremind.data.mapper.toDomain
import com.andres.pillremind.data.mapper.toEntity
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.repository.MedicationRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Clase que implemnta la interfaz  MedicamentoRepository
 */
class MedicationRepositoryImpl @Inject constructor(private val medicationDao: MedicationDao) :
    MedicationRepository {

    override suspend fun insertMedication(medication: Medication): Long {
        return medicationDao.insertMedication(medication.toEntity())
    }

    override suspend fun deleteMedication(medication: Medication) {
        medicationDao.deleteMedication(medication.toEntity())
    }

    override suspend fun updateMedication(medication: Medication) {
        medicationDao.updateMedication(medication.toEntity())
    }

    override fun getMedicationById(medicationId: Int): Flow<Medication> {
        return medicationDao.getMedicationById(medicationId).map { it.toDomain() }
    }

    override fun getMedications(profileId: Int): Flow<List<Medication>> {
        return medicationDao.getMedications(profileId).map { list -> list.map { it.toDomain() } }
    }
}