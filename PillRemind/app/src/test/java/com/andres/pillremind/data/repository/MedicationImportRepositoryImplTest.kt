package com.andres.pillremind.data.repository

import com.andres.pillremind.data.pdf.PdfImportException
import com.andres.pillremind.data.pdf.PdfTextExtractor
import com.andres.pillremind.domain.model.MedicationImportError
import com.andres.pillremind.domain.model.MedicationImportResult
import io.mockk.coEvery
import io.mockk.mockk
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del contrato de errores de la importación de PDF.
 *
 * La extracción se sustituye por un mock para probar el mapeo de errores sin
 * depender de PdfBox.
 */
class MedicationImportRepositoryImplTest {

    private val extractor = mockk<PdfTextExtractor>()
    private val repository = MedicationImportRepositoryImpl(extractor)

    @Test
    fun `un pdf sin texto se convierte en error sin texto`() {
        coEvery { extractor.extract(any()) } returns "   \n"

        val result = runBlocking { repository.parsePdf(emptyInput()) }

        assertEquals(
            MedicationImportResult.Error(MedicationImportError.NO_TEXT),
            result
        )
    }

    @Test
    fun `un texto sin medicamentos se convierte en error sin medicamentos`() {
        coEvery { extractor.extract(any()) } returns
            """
            Folla de Medicación Activa
            Paciente: PACIENTE DE PRUEBA
            """.trimIndent()

        val result = runBlocking { repository.parsePdf(emptyInput()) }

        assertEquals(
            MedicationImportResult.Error(MedicationImportError.NO_MEDICATIONS),
            result
        )
    }

    @Test
    fun `un pdf valido devuelve los medicamentos interpretados`() {
        coEvery { extractor.extract(any()) } returns
            """
            ENALAPRIL 5MG 28 COMPRIMIDOS
            1 COMPRIMIDO AO DÍA
            """.trimIndent()

        val result = runBlocking { repository.parsePdf(emptyInput()) }

        assertTrue(result is MedicationImportResult.Success)
        val medication = (result as MedicationImportResult.Success).medications.single()
        assertEquals("ENALAPRIL 5MG", medication.name)
        assertEquals("1", medication.doseText)
        assertEquals("comprimidos", medication.unit)
    }

    @Test
    fun `conserva el motivo de un pdf cifrado`() {
        coEvery { extractor.extract(any()) } throws
            PdfImportException(MedicationImportError.ENCRYPTED)

        val result = runBlocking { repository.parsePdf(emptyInput()) }

        assertEquals(
            MedicationImportResult.Error(MedicationImportError.ENCRYPTED),
            result
        )
    }

    @Test
    fun `un fallo inesperado se convierte en error de lectura`() {
        coEvery { extractor.extract(any()) } throws IllegalStateException("fallo simulado")

        val result = runBlocking { repository.parsePdf(emptyInput()) }

        assertEquals(
            MedicationImportResult.Error(MedicationImportError.IO),
            result
        )
    }

    @Test
    fun `la cancelacion no se convierte en error de lectura`() {
        coEvery { extractor.extract(any()) } throws CancellationException("cancelado")

        assertThrows(CancellationException::class.java) {
            runBlocking { repository.parsePdf(emptyInput()) }
        }
    }

    private fun emptyInput(): InputStream = ByteArrayInputStream(byteArrayOf(1))
}
