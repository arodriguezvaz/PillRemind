package com.andres.pillremind.ui.screens.profile

import android.net.Uri
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.pdf.AdherencePdfFileStore
import com.andres.pillremind.data.pdf.AdherencePdfGenerator
import com.andres.pillremind.data.pdf.AdherencePdfLabels
import com.andres.pillremind.data.pdf.AdherencePdfReport
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.model.StatisticsIntake
import com.andres.pillremind.domain.model.StatisticsPeriod
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Pruebas del ViewModel que exporta el informe de adherencia del perfil.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileReportViewModelTest {

    private val now = LocalDateTime.of(2026, 11, 17, 12, 0)
    private val intakeRepository = mockk<IntakeRepository>()
    private val profileRepository = mockk<ProfileRepository>()
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val adherencePdfGenerator = mockk<AdherencePdfGenerator>()
    private val adherencePdfFileStore = mockk<AdherencePdfFileStore>(relaxed = true)

    private lateinit var viewModel: ProfileReportViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { sessionManager.activeProfileId } returns flowOf(1)
        coEvery { profileRepository.getProfileByIdOnce(1) } returns Profile(
            id = 1,
            usuarioId = 1,
            name = "Ana"
        )
        every { intakeRepository.observeStatisticsIntakes(any(), any(), any()) } returns flowOf(
            listOf(
                intake(
                    id = 1,
                    scheduledAt = now.minusDays(1).withHour(9).withMinute(0),
                    takenAt = now.minusDays(1).withHour(9).withMinute(5)
                ),
                intake(
                    id = 2,
                    scheduledAt = now.minusDays(1).withHour(21).withMinute(0)
                )
            )
        )
        viewModel = ProfileReportViewModel(
            intakeRepository = intakeRepository,
            profileRepository = profileRepository,
            sessionManager = sessionManager,
            adherencePdfGenerator = adherencePdfGenerator,
            adherencePdfFileStore = adherencePdfFileStore
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `el periodo inicial es de treinta dias`() {
        assertEquals(StatisticsPeriod.THIRTY, viewModel.period.value)
    }

    @Test
    fun `exporta el informe del periodo seleccionado`() = runTest {
        val uri = mockk<Uri>()
        val output = ByteArrayOutputStream()
        every { adherencePdfFileStore.openOutput(uri) } returns output
        every { adherencePdfGenerator.write(any(), any(), any(), any()) } just Runs

        viewModel.selectPeriod(StatisticsPeriod.SEVEN)
        viewModel.exportPdf(uri, pdfLabels(), now = now)

        coVerify(timeout = 2_000) {
            intakeRepository.observeStatisticsIntakes(
                profileId = 1,
                from = now.toLocalDate().minusDays(6).atStartOfDay(),
                to = now.toLocalDate().atTime(LocalTime.MAX)
            )
        }
        val reportSlot = slot<AdherencePdfReport>()
        verify(timeout = 2_000) {
            adherencePdfGenerator.write(
                output = output,
                report = capture(reportSlot),
                labels = any(),
                locale = any()
            )
        }

        val report = reportSlot.captured
        assertEquals(StatisticsPeriod.SEVEN, report.period)
        assertEquals("Ana", report.profileName)
        assertEquals(1, report.report.summary.taken)
        assertEquals(1, report.report.summary.missed)
        assertEquals(50, report.report.summary.adherencePercent)
    }

    @Test
    fun `borra el archivo parcial cuando falla la exportacion`() = runTest {
        val uri = mockk<Uri>()
        every { adherencePdfFileStore.openOutput(uri) } throws IOException("sin espacio")

        viewModel.exportPdf(uri, pdfLabels(), now = now)

        verify(timeout = 2_000) { adherencePdfFileStore.delete(uri) }
    }

    private fun intake(
        id: Int,
        scheduledAt: LocalDateTime,
        takenAt: LocalDateTime? = null
    ): StatisticsIntake = StatisticsIntake(
        intakeId = id,
        medicationId = 1,
        scheduledAt = scheduledAt,
        takenAt = takenAt,
        medicationName = "Ibuprofeno",
        dose = 600.0,
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
        disclaimer = "Datos registrados por el usuario.",
        footer = "Generado con PillRemind",
        pageFormat = "Página %1\$d de %2\$d",
        unknownProfile = "Perfil sin nombre",
        percentPattern = "%1\$d%%",
        datePattern = "dd/MM/yyyy"
    )
}
