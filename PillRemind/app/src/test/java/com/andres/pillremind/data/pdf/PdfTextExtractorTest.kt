package com.andres.pillremind.data.pdf

import com.andres.pillremind.domain.model.MedicationImportError
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas de la extracción de texto con PdfBox.
 *
 * Los PDFs se generan en el propio test: no se usan documentos reales ni datos
 * de ningún paciente.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PdfTextExtractorTest {

    private val extractor = PdfTextExtractor()

    @Test
    fun `extrae el texto de un pdf con capa de texto`() {
        val text = runBlocking {
            extractor.extract(ByteArrayInputStream(textPdf("ENALAPRIL 5MG")))
        }

        assertEquals("ENALAPRIL 5MG", text.trim())
    }

    @Test
    fun `devuelve texto vacio cuando el pdf no tiene capa de texto`() {
        val text = runBlocking {
            extractor.extract(ByteArrayInputStream(blankPdf(pages = 1)))
        }

        assertTrue("Un PDF escaneado debe quedar sin texto", text.isBlank())
    }

    @Test
    fun `rechaza un pdf cifrado con el error tipado`() {
        val exception = assertThrows(PdfImportException::class.java) {
            runBlocking {
                extractor.extract(ByteArrayInputStream(encryptedPdf()))
            }
        }

        assertEquals(MedicationImportError.ENCRYPTED, exception.error)
    }

    @Test
    fun `rechaza un pdf con mas de veinte paginas`() {
        val exception = assertThrows(PdfImportException::class.java) {
            runBlocking {
                extractor.extract(ByteArrayInputStream(blankPdf(pages = 21)))
            }
        }

        assertEquals(MedicationImportError.TOO_MANY_PAGES, exception.error)
    }

    @Test
    fun `acepta un pdf de exactamente veinte paginas`() {
        val text = runBlocking {
            extractor.extract(ByteArrayInputStream(blankPdf(pages = 20)))
        }

        assertTrue(text.isBlank())
    }

    @Test
    fun `marca como corrupto un flujo que no es un pdf`() {
        val exception = assertThrows(PdfImportException::class.java) {
            runBlocking {
                extractor.extract(ByteArrayInputStream(byteArrayOf(1, 2, 3, 4, 5)))
            }
        }

        assertEquals(MedicationImportError.CORRUPT, exception.error)
    }

    private fun textPdf(text: String): ByteArray = documentBytes { document ->
        val page = PDPage()
        document.addPage(page)
        PDPageContentStream(document, page).use { content ->
            content.beginText()
            content.setFont(PDType1Font.HELVETICA, 12f)
            content.newLineAtOffset(50f, 700f)
            content.showText(text)
            content.endText()
        }
    }

    private fun blankPdf(pages: Int): ByteArray = documentBytes { document ->
        repeat(pages) { document.addPage(PDPage()) }
    }

    private fun encryptedPdf(): ByteArray = documentBytes { document ->
        document.addPage(PDPage())
        document.protect(StandardProtectionPolicy("owner", "user", AccessPermission()))
    }

    private fun documentBytes(build: (PDDocument) -> Unit): ByteArray =
        PDDocument().use { document ->
            build(document)
            ByteArrayOutputStream().use { output ->
                document.save(output)
                output.toByteArray()
            }
        }
}
