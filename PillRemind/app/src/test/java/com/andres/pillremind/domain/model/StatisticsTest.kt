package com.andres.pillremind.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del cálculo puro de estadísticas.
 *
 * El caso importante es que las tomas pendientes no penalizan la adherencia y
 * que el margen de 15 minutos coincide con el estado visual de Inicio.
 */
class StatisticsTest {

    private val now = LocalDateTime.of(2026, 11, 5, 12, 0)

    @Test
    fun `una toma registrada siempre cuenta como tomada`() {
        val intake = intake(
            id = 1,
            scheduledAt = now.minusHours(2),
            takenAt = now.minusMinutes(15)
        )

        assertEquals(StatisticsStatus.TAKEN, intake.status(now))
    }

    @Test
    fun `una toma con mas de quince minutos sin registrar esta omitida`() {
        val intake = intake(id = 1, scheduledAt = now.minusMinutes(16))

        assertEquals(StatisticsStatus.MISSED, intake.status(now))
    }

    @Test
    fun `una toma en el limite de quince minutos sigue pendiente`() {
        val intake = intake(id = 1, scheduledAt = now.minusMinutes(15))

        assertEquals(StatisticsStatus.PENDING, intake.status(now))
    }

    @Test
    fun `una toma futura esta pendiente`() {
        val intake = intake(id = 1, scheduledAt = now.plusMinutes(1))

        assertEquals(StatisticsStatus.PENDING, intake.status(now))
    }

    @Test
    fun `el resumen no penaliza las tomas pendientes`() {
        val intakes = listOf(
            intake(id = 1, scheduledAt = now.minusHours(3), takenAt = now.minusHours(2)),
            intake(id = 2, scheduledAt = now.minusHours(2), takenAt = now.minusHours(1)),
            intake(id = 3, scheduledAt = now.minusMinutes(20)),
            intake(id = 4, scheduledAt = now.plusMinutes(10)),
            intake(id = 5, scheduledAt = now.plusHours(1)),
            intake(id = 6, scheduledAt = now.plusHours(2))
        )

        val summary = intakes.toStatisticsSummary(now)

        assertEquals(2, summary.taken)
        assertEquals(1, summary.missed)
        assertEquals(3, summary.pending)
        assertEquals(3, summary.evaluated)
        assertEquals(6, summary.total)
        assertEquals(67, summary.adherencePercent)
    }

    @Test
    fun `sin tomas evaluadas no hay porcentaje`() {
        val intakes = listOf(
            intake(id = 1, scheduledAt = now.plusMinutes(10)),
            intake(id = 2, scheduledAt = now.plusHours(1))
        )

        val summary = intakes.toStatisticsSummary(now)

        assertEquals(0, summary.evaluated)
        assertNull(summary.adherencePercent)
    }

    @Test
    fun `el informe agrupa por medicamento y deja los activos primero`() {
        val intakes = listOf(
            intake(id = 1, medicationId = 2, scheduledAt = now, name = "Omeprazol", archived = true),
            intake(id = 2, medicationId = 1, scheduledAt = now, name = "Ibuprofeno", takenAt = now),
            intake(id = 3, medicationId = 1, name = "Ibuprofeno", scheduledAt = now.minusHours(2)),
            intake(id = 4, medicationId = 1, name = "Ibuprofeno", scheduledAt = now.plusHours(1))
        )

        val report = intakes.toStatisticsReport(now)

        assertEquals(2, report.byMedication.size)
        assertEquals("Ibuprofeno", report.byMedication[0].medicationName)
        assertEquals(1, report.byMedication[0].taken)
        assertEquals(1, report.byMedication[0].missed)
        assertEquals(1, report.byMedication[0].pending)
        assertEquals("Omeprazol", report.byMedication[1].medicationName)
        assertTrue(report.byMedication[1].isArchived)
    }

    @Test
    fun `el informe usa el snapshot mas reciente del medicamento`() {
        val intakes = listOf(
            intake(
                id = 1,
                medicationId = 1,
                scheduledAt = now.minusDays(2),
                name = "Ibuprofeno",
                dose = 400.0
            ),
            intake(
                id = 2,
                medicationId = 1,
                scheduledAt = now.minusDays(1),
                name = "Ibuprofeno",
                dose = 600.0
            )
        )

        val report = intakes.toStatisticsReport(now)

        assertEquals("Ibuprofeno", report.byMedication.single().medicationName)
        assertEquals(600.0, report.byMedication.single().dose, 0.0)
    }

    @Test
    fun `el grafico incluye siete dias aunque alguno este vacio`() {
        val intakes = listOf(
            intake(
                id = 1,
                scheduledAt = now.withHour(9).withMinute(0),
                takenAt = now.withHour(9).withMinute(5)
            ),
            intake(
                id = 2,
                scheduledAt = now.minusDays(1).withHour(21).withMinute(0),
                takenAt = now.minusDays(1)
            )
        )

        val report = intakes.toStatisticsReport(now)

        assertEquals(7, report.lastSevenDays.size)
        assertEquals(LocalDate.of(2026, 10, 30), report.lastSevenDays.first().date)
        assertEquals(LocalDate.of(2026, 11, 5), report.lastSevenDays.last().date)
        assertEquals(1, report.lastSevenDays.last().taken)
        assertEquals(0, report.lastSevenDays[4].total)
    }

    @Test
    fun `el grafico excluye las tomas anteriores a siete dias`() {
        val intakes = listOf(
            intake(id = 1, scheduledAt = now.minusDays(7)),
            intake(id = 2, scheduledAt = now.withHour(9).withMinute(0))
        )

        val report = intakes.toStatisticsReport(now)

        assertEquals(2, report.summary.total)
        assertEquals(7, report.lastSevenDays.size)
        assertEquals(1, report.lastSevenDays.sumOf { it.total })
    }

    @Test
    fun `el historial ordena de mas reciente a mas antiguo`() {
        val intakes = listOf(
            intake(id = 1, scheduledAt = now.minusDays(2)),
            intake(id = 2, scheduledAt = now.withHour(9).withMinute(0)),
            intake(id = 3, scheduledAt = now.minusDays(1))
        )

        val report = intakes.toStatisticsReport(now)

        assertEquals(listOf(2, 3, 1), report.history.map { it.intakeId })
    }

    @Test
    fun `una lista vacia genera un informe vacio`() {
        val report = emptyList<StatisticsIntake>().toStatisticsReport(now)

        assertEquals(0, report.summary.total)
        assertNull(report.summary.adherencePercent)
        assertTrue(report.byMedication.isEmpty())
        assertTrue(report.history.isEmpty())
        assertEquals(7, report.lastSevenDays.size)
        assertTrue(report.lastSevenDays.all { it.total == 0 })
    }

    private fun intake(
        id: Int,
        medicationId: Int = 1,
        scheduledAt: LocalDateTime,
        takenAt: LocalDateTime? = null,
        name: String = "Ibuprofeno",
        dose: Double = 600.0,
        archived: Boolean = false
    ): StatisticsIntake = StatisticsIntake(
        intakeId = id,
        medicationId = medicationId,
        scheduledAt = scheduledAt,
        takenAt = takenAt,
        medicationName = name,
        dose = dose,
        unit = "mg",
        imageUrl = null,
        isArchived = archived
    )
}
