package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.MedicationImportResult
import java.io.InputStream

/**
 * Lee una hoja de medicación en PDF y devuelve los medicamentos detectados.
 *
 * El dominio no conoce Android: recibe un [InputStream] y devuelve modelos
 * efímeros que la pantalla de revisión convierte en medicamentos reales.
 */
interface MedicationImportRepository {

    /**
     * Extrae y normaliza los medicamentos del PDF.
     *
     * @param input Flujo del documento seleccionado.
     */
    suspend fun parsePdf(input: InputStream): MedicationImportResult
}
