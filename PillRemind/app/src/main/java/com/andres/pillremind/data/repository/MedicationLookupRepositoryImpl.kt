package com.andres.pillremind.data.repository

import com.andres.pillremind.data.remote.CimaDataSource
import com.andres.pillremind.domain.model.CnLookupResult
import com.andres.pillremind.domain.repository.MedicationLookupRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/**
 * Implementación de la consulta de medicamentos por CN.
 */
class MedicationLookupRepositoryImpl @Inject constructor(
    private val dataSource: CimaDataSource
) : MedicationLookupRepository {

    override suspend fun findByCn(cn: String): CnLookupResult = try {
        val medication = dataSource.findByCn(cn)
        if (medication == null) {
            CnLookupResult.NotFound(cn)
        } else {
            CnLookupResult.Found(medication)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        CnLookupResult.Error(cn, e)
    }
}
