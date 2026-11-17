package com.andres.pillremind.data.pdf

import com.andres.pillremind.domain.model.StatisticsIntake
import com.andres.pillremind.domain.model.StatisticsPeriod
import com.andres.pillremind.domain.model.toStatisticsReport
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.util.Locale
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas del informe de adherencia en PDF.
 *
 * Los informes se generan en el propio test con datos ficticios y después se
 * vuelve a extraer el texto para comprobar el contenido y la paginación.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AdherencePdfGeneratorTest {

    private val generator = AdherencePdfGenerator()
    private val locale = Locale.forLanguageTag("es-ES")
    private val now = LocalDateTime.of(2026, 11, 17, 12, 0)
    private val labels = pdfLabels()

    @Test
    fun `incluye cabecera, resumen, tabla y aviso`() {
        val intakes = listOf(
            intake(
                id = 1,
                medicationName = "Ácido acetilsalicílico",
                dose = 100.0,
                scheduledAt = now.minusDays(1).withHour(9).withMinute(0),
                takenAt = now.minusDays(1).withHour(9).withMinute(5)
            ),
            intake(
                id = 2,
                medicationName = "Ácido acetilsalicílico",
                dose = 100.0,
                scheduledAt = now.minusDays(1).withHour(21).withMinute(0)
            ),
            intake(
                id = 3,
                medicationId = 2,
                medicationName = "Omeprazol",
                dose = 20.0,
                scheduledAt = now.minusDays(1).withHour(9).withMinute(0),
                takenAt = now.minusDays(1).withHour(9).withMinute(1)
            ),
            intake(
                id = 4,
                medicationId = 3,
                medicationName = "Ibuprofeno",
                scheduledAt = now.minusDays(2).withHour(9).withMinute(0),
                takenAt = now.minusDays(2).withHour(9).withMinute(2)
            )
        )

        val text = textOf(generate(report("María Núñez", intakes)))
        assertContains(text, "Informe de adherencia")
        assertContains(text, "Perfil: María Núñez")
        assertContains(text, "Periodo: 19/10/2026 – 17/11/2026")
        assertContains(text, "75%")
        assertContains(text, "Tomadas: 3")
        assertContains(text, "Sin registrar: 1")
        assertContains(text, "Pendientes: 0")
        assertContains(text, "Tomas evaluadas: 4")
        assertContains(text, "Por medicamento")
        assertContains(text, "Medicamento")
        assertContains(text, "Ácido acetilsalicílico")
        assertContains(text, "100 mg")
        assertContains(text, "Omeprazol")
        assertContains(text, "20 mg")
        assertContains(
            text,
            "Datos registrados por el usuario en PillRemind. Una toma sin registrar no implica que no se haya tomado."
        )
        assertContains(text, "Página 1 de 1")
    }

    @Test
    fun `pagina la lista de tomas sin registrar`() {
        val intakes = (1..200).map { index ->
            intake(
                id = index,
                scheduledAt = now.minusHours((index + 1).toLong()).withMinute(0)
            )
        }

        val bytes = generate(report("Ana", intakes))

        PDDocument.load(ByteArrayInputStream(bytes)).use { document ->
            assertTrue("La lista larga debe ocupar varias páginas", document.numberOfPages > 1)
        }
        val text = textOf(bytes)
        assertContains(text, "Tomas sin registrar (200)")
        assertContains(text, "Página 2 de")
    }

    @Test
    fun `sin tomas evaluadas muestra sin datos`() {
        val intakes = listOf(
            intake(
                id = 1,
                scheduledAt = now.plusHours(2).withMinute(0)
            )
        )

        val text = textOf(generate(report("Ana", intakes)))

        assertContains(text, "Sin datos")
        assertContains(text, "Sin registrar: 0")
        assertContains(text, "Pendientes: 1")
    }

    @Test
    fun `no falla con caracteres fuera de winansi`() {
        val intakes = listOf(
            intake(
                id = 1,
                medicationName = "Ibuprofeno 😀",
                scheduledAt = now.minusDays(1).withHour(9).withMinute(0)
            )
        )

        val text = textOf(generate(report("Ana 😀", intakes)))

        assertContains(text, "Ibuprofeno ?")
        assertContains(text, "Perfil: Ana ?")
    }

    @Test
    fun `indica cuando no hay tomas sin registrar`() {
        val intakes = listOf(
            intake(
                id = 1,
                scheduledAt = now.minusDays(1).withHour(9).withMinute(0),
                takenAt = now.minusDays(1).withHour(9).withMinute(5)
            )
        )

        val text = textOf(generate(report("Ana", intakes)))

        assertContains(text, "No hay tomas sin registrar en este periodo.")
    }

    private fun report(profileName: String, intakes: List<StatisticsIntake>): AdherencePdfReport =
        AdherencePdfReport(
            profileName = profileName,
            period = StatisticsPeriod.THIRTY,
            reference = now,
            report = intakes.toStatisticsReport(now, chartDays = StatisticsPeriod.THIRTY.days.toInt())
        )

    private fun generate(report: AdherencePdfReport): ByteArray {
        val output = ByteArrayOutputStream()
        generator.write(output, report, labels, locale)
        return output.toByteArray()
    }

    private fun textOf(bytes: ByteArray): String =
        PDDocument.load(ByteArrayInputStream(bytes)).use { document ->
            PDFTextStripper().apply { sortByPosition = true }.getText(document)
        }.replace(Regex("\\s+"), " ").trim()

    private fun assertContains(text: String, expected: String) {
        assertTrue("No se encontró «$expected» en el informe:\n$text", text.contains(expected))
    }

    private fun intake(
        id: Int,
        medicationId: Int = 1,
        medicationName: String = "Ibuprofeno",
        dose: Double = 600.0,
        scheduledAt: LocalDateTime,
        takenAt: LocalDateTime? = null
    ): StatisticsIntake = StatisticsIntake(
        intakeId = id,
        medicationId = medicationId,
        scheduledAt = scheduledAt,
        takenAt = takenAt,
        medicationName = medicationName,
        dose = dose,
        unit = "mg",
        imageUrl = null,
        isArchived = false
    )

    private fun pdfLabels(): AdherencePdfLabels = AdherencePdfLabels(
        title = "Informe de adherencia",
        profileLabel = "Perfil",
        periodLabel = "Periodo",
        generatedAtLabel = "Generado el",
        adherenceLabel = "Adherencia",
        evaluatedLabel = "Tomas evaluadas",
        takenLabel = "Tomadas",
        missedLabel = "Sin registrar",
        pendingLabel = "Pendientes",
        noDataLabel = "Sin datos",
        archivedLabel = "Archivado",
        byMedicationTitle = "Por medicamento",
        medicationColumn = "Medicamento",
        missedTitle = "Tomas sin registrar",
        missedEmpty = "No hay tomas sin registrar en este periodo.",
        disclaimer = "Datos registrados por el usuario en PillRemind. Una toma sin registrar no implica que no se haya tomado.",
        footer = "Generado con PillRemind",
        pageFormat = "Página %1\$d de %2\$d",
        unknownProfile = "Perfil sin nombre",
        percentPattern = "%1\$d%%",
        datePattern = "dd/MM/yyyy"
    )
}
