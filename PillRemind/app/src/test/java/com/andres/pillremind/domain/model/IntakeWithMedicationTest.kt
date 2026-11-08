package com.andres.pillremind.domain.model

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del estado visual de las tomas y de la detección de tomas de
 * madrugada que Inicio mantiene accesibles.
 */
class IntakeWithMedicationTest {

    private val scheduledAt = LocalDateTime.of(2026, 11, 7, 23, 0)

    @Test
    fun `una toma registrada siempre esta tomada`() {
        val intake = intake(takenAt = scheduledAt.plusDays(1))

        assertEquals(
            IntakeVisualState.TAKEN,
            intake.visualState(scheduledAt.plusDays(2))
        )
    }

    @Test
    fun `una toma dentro del margen esta pendiente`() {
        val intake = intake(takenAt = null)

        assertEquals(
            IntakeVisualState.PENDING,
            intake.visualState(scheduledAt.plusMinutes(14))
        )
    }

    @Test
    fun `una toma pasada del margen esta retrasada`() {
        val intake = intake(takenAt = null)

        assertEquals(
            IntakeVisualState.DELAYED,
            intake.visualState(scheduledAt.plusMinutes(16))
        )
    }

    @Test
    fun `una toma pendiente justo en el limite de quince minutos sigue pendiente`() {
        val intake = intake(takenAt = null)

        assertEquals(
            IntakeVisualState.PENDING,
            intake.visualState(scheduledAt.plusMinutes(15))
        )
    }

    @Test
    fun `una toma pendiente necesita revision`() {
        assertTrue(intake(takenAt = null).isPendingOrTakenLate())
    }

    @Test
    fun `una toma registrada el mismo dia no necesita revision`() {
        assertFalse(
            intake(takenAt = scheduledAt.plusMinutes(5)).isPendingOrTakenLate()
        )
    }

    @Test
    fun `una toma registrada de madrugada necesita revision`() {
        val takenAfterMidnight = scheduledAt.toLocalDate().plusDays(1).atTime(0, 25)

        assertTrue(intake(takenAt = takenAfterMidnight).isPendingOrTakenLate())
    }

    @Test
    fun `una toma registrada justo al cambiar de dia necesita revision`() {
        val takenAtMidnight = scheduledAt.toLocalDate().plusDays(1).atStartOfDay()

        assertTrue(intake(takenAt = takenAtMidnight).isPendingOrTakenLate())
    }

    @Test
    fun `una toma registrada tarde pero el mismo dia no necesita revision`() {
        val scheduled = LocalDateTime.of(2026, 11, 7, 9, 0)

        assertFalse(
            intake(
                scheduled = scheduled,
                takenAt = scheduled.toLocalDate().atTime(22, 0)
            ).isPendingOrTakenLate()
        )
    }

    @Test
    fun `una toma registrada antes del dia programado no necesita revision`() {
        assertFalse(
            intake(takenAt = scheduledAt.minusDays(1)).isPendingOrTakenLate()
        )
    }

    private fun intake(
        takenAt: LocalDateTime?,
        scheduled: LocalDateTime = scheduledAt
    ): IntakeWithMedication =
        IntakeWithMedication(
            intakeId = 1,
            medicationId = 1,
            scheduledAt = scheduled,
            scheduleId = 1,
            takenAt = takenAt,
            medicationName = "Ibuprofeno",
            dose = 600.0,
            unit = "mg",
            imageUrl = null,
            profileName = "Andrés"
        )
}
