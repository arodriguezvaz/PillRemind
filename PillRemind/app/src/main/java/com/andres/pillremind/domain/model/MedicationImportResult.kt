package com.andres.pillremind.domain.model

/**
 * Resultado de interpretar un PDF de medicación.
 */
sealed interface MedicationImportResult {

    /**
     * El PDF se ha leído y contiene al menos un medicamento.
     */
    data class Success(val medications: List<ImportedMedication>) : MedicationImportResult

    /**
     * No se ha podido interpretar el PDF.
     */
    data class Error(val error: MedicationImportError) : MedicationImportResult
}

/**
 * Motivos por los que puede fallar la importación de un PDF.
 */
enum class MedicationImportError {
    NO_TEXT,
    NO_MEDICATIONS,
    ENCRYPTED,
    TOO_LARGE,
    TOO_MANY_PAGES,
    CORRUPT,
    IO
}
