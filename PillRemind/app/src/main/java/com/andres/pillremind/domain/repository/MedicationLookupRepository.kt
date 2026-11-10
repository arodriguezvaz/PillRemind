package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.CnLookupResult

/**
 * Busca un medicamento por Código Nacional en la API pública de CIMA.
 */
interface MedicationLookupRepository {
    suspend fun findByCn(cn: String): CnLookupResult
}
