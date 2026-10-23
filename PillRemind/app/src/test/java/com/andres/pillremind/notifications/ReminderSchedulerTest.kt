package com.andres.pillremind.notifications

import com.andres.pillremind.domain.model.Intake
import com.andres.pillremind.domain.repository.IntakeRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Pruebas de la programación de alarmas.
 *
 * Se comprueba que no se programen tomas pasadas ni tomas con ID inválido,
 * y que la generación y la cancelación usen las alarmas correctas.
 */
class ReminderSchedulerTest {

    private val intakeRepository = mockk<IntakeRepository>()
    private val alarmScheduler = mockk<AlarmScheduler>(relaxed = true)
    private val scheduler = ReminderScheduler(intakeRepository, alarmScheduler)

    private val now = LocalDateTime.of(2026, 10, 23, 12, 0)
    private val today: LocalDate = now.toLocalDate()

    @Test
    fun `scheduleFuture solo programa tomas futuras con ID valido`() {
        val future = Intake(
            id = 1,
            scheduleId = 10,
            scheduledAt = now.plusHours(1)
        )
        val past = Intake(
            id = 2,
            scheduleId = 10,
            scheduledAt = now.minusHours(1)
        )
        val invalid = Intake(
            id = -1,
            scheduleId = 10,
            scheduledAt = now.plusHours(2)
        )

        scheduler.scheduleFuture(listOf(future, past, invalid), now)

        verify(exactly = 1) { alarmScheduler.schedule(1, any()) }
        verify(exactly = 0) { alarmScheduler.schedule(2, any()) }
        verify(exactly = 0) { alarmScheduler.schedule(-1, any()) }
    }

    @Test
    fun `generateAndSchedule genera tomas y programa sus alarmas`() = runTest {
        val future = Intake(
            id = 1,
            scheduleId = 10,
            scheduledAt = now.plusHours(1)
        )
        coEvery { intakeRepository.createIntakes(today, today) } returns listOf(future)

        scheduler.generateAndSchedule(today, today, now)

        coVerify(exactly = 1) { intakeRepository.createIntakes(today, today) }
        verify(exactly = 1) { alarmScheduler.schedule(1, any()) }
    }

    @Test
    fun `generateAndSchedule corrige un rango invertido`() = runTest {
        val tomorrow = today.plusDays(1)
        coEvery { intakeRepository.createIntakes(today, today) } returns emptyList()

        scheduler.generateAndSchedule(tomorrow, today, now)

        coVerify(exactly = 1) { intakeRepository.createIntakes(today, today) }
    }

    @Test
    fun `generateWindow genera siete dias desde hoy y guarda el fin de ventana`() = runTest {
        val end = today.plusDays(ReminderScheduler.WINDOW_DAYS - 1)
        val future = Intake(
            id = 1,
            scheduleId = 10,
            scheduledAt = now.plusHours(1)
        )
        coEvery { intakeRepository.getLastGeneratedDate() } returns today
        coEvery { intakeRepository.createIntakes(today, end) } returns listOf(future)
        coEvery { intakeRepository.saveLastGeneratedDate(end) } just Runs

        scheduler.generateWindow(today, now)

        coVerify(exactly = 1) { intakeRepository.createIntakes(today, end) }
        coVerify(exactly = 1) { intakeRepository.saveLastGeneratedDate(end) }
        verify(exactly = 1) { alarmScheduler.schedule(1, any()) }
    }

    @Test
    fun `generateWindow normaliza una ultima generacion futura`() = runTest {
        val end = today.plusDays(ReminderScheduler.WINDOW_DAYS - 1)
        coEvery { intakeRepository.getLastGeneratedDate() } returns today.plusDays(3)
        coEvery { intakeRepository.createIntakes(today, end) } returns emptyList()
        coEvery { intakeRepository.saveLastGeneratedDate(end) } just Runs

        scheduler.generateWindow(today, now)

        coVerify(exactly = 1) { intakeRepository.createIntakes(today, end) }
    }

    @Test
    fun `generateWindow conserva el inicio si la ultima generacion es pasada`() = runTest {
        val start = today.minusDays(2)
        val end = today.plusDays(ReminderScheduler.WINDOW_DAYS - 1)
        coEvery { intakeRepository.getLastGeneratedDate() } returns start
        coEvery { intakeRepository.createIntakes(start, end) } returns emptyList()
        coEvery { intakeRepository.saveLastGeneratedDate(end) } just Runs

        scheduler.generateWindow(today, now)

        coVerify(exactly = 1) { intakeRepository.createIntakes(start, end) }
    }

    @Test
    fun `windowEnd devuelve el septimo dia de la ventana`() {
        assertEquals(today.plusDays(6), scheduler.windowEnd(today))
    }

    @Test
    fun `cancelFutureForMedication cancela las alarmas de las tomas futuras`() = runTest {
        val intakes = listOf(
            Intake(id = 1, scheduleId = 10, scheduledAt = now.plusHours(1)),
            Intake(id = 2, scheduleId = 10, scheduledAt = now.plusHours(2))
        )
        coEvery {
            intakeRepository.getFutureIntakesByMedicationId(10, now)
        } returns intakes

        scheduler.cancelFutureForMedication(10, now)

        verify(exactly = 1) { alarmScheduler.cancel(1) }
        verify(exactly = 1) { alarmScheduler.cancel(2) }
    }
}
