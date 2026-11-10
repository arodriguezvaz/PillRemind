package com.andres.pillremind.data.pdf

import com.andres.pillremind.domain.model.MedicationImportError
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extrae la capa de texto de un PDF con PdfBox.
 *
 * No hace OCR: si el PDF solo contiene imágenes, el texto saldrá vacío y la
 * capa de repositorio lo convertirá en un error comprensible para el usuario.
 */
@Singleton
class PdfTextExtractor @Inject constructor() {

    /**
     * Lee el texto del documento, ordenado por posición.
     *
     * @param input Flujo del PDF elegido con Storage Access Framework.
     * @throws PdfImportException Si el PDF está cifrado, es demasiado largo o
     * no se puede leer.
     */
    suspend fun extract(input: InputStream): String = withContext(Dispatchers.IO) {
        try {
            val document = PDDocument.load(input)
            try {
                if (document.numberOfPages > MAX_PAGES) {
                    throw PdfImportException(MedicationImportError.TOO_MANY_PAGES)
                }

                PDFTextStripper().apply {
                    sortByPosition = true
                }.getText(document)
            } finally {
                document.close()
            }
        } catch (e: InvalidPasswordException) {
            throw PdfImportException(MedicationImportError.ENCRYPTED)
        } catch (e: PdfImportException) {
            throw e
        } catch (e: IOException) {
            throw PdfImportException(MedicationImportError.CORRUPT)
        } catch (e: Exception) {
            throw PdfImportException(MedicationImportError.CORRUPT)
        }
    }

    private companion object {
        const val MAX_PAGES = 20
    }
}

/**
 * Error tipado de la extracción de PDF.
 */
class PdfImportException(
    val error: MedicationImportError
) : Exception(error.name)
