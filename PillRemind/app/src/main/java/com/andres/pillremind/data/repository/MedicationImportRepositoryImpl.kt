package com.andres.pillremind.data.repository

import com.andres.pillremind.data.pdf.PdfImportException
import com.andres.pillremind.data.pdf.PdfTextExtractor
import com.andres.pillremind.domain.model.MedicationImportError
import com.andres.pillremind.domain.model.MedicationImportResult
import com.andres.pillremind.domain.parsing.MedicationListParser
import com.andres.pillremind.domain.repository.MedicationImportRepository
import java.io.InputStream
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/**
 * Implementación de la importación de PDF.
 */
class MedicationImportRepositoryImpl @Inject constructor(
    private val textExtractor: PdfTextExtractor
) : MedicationImportRepository {

    override suspend fun parsePdf(input: InputStream): MedicationImportResult {
        return try {
            val text = textExtractor.extract(input)
            if (text.isBlank()) {
                MedicationImportResult.Error(MedicationImportError.NO_TEXT)
            } else {
                val medications = MedicationListParser.parse(text)
                if (medications.isEmpty()) {
                    MedicationImportResult.Error(MedicationImportError.NO_MEDICATIONS)
                } else {
                    MedicationImportResult.Success(medications)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: PdfImportException) {
            MedicationImportResult.Error(e.error)
        } catch (e: Exception) {
            MedicationImportResult.Error(MedicationImportError.IO)
        }
    }
}
