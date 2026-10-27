package com.andres.pillremind.data.repository

import com.andres.pillremind.data.local.dao.IntakeDao
import com.andres.pillremind.data.local.dao.MedicationDao
import com.andres.pillremind.data.local.dao.ScheduleDao
import com.andres.pillremind.data.local.entity.IntakeEntity
import com.andres.pillremind.data.local.entity.MedicationEntity
import com.andres.pillremind.data.local.entity.ScheduleEntity
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.WeekDay
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Pruebas de la generación de tomas.
 *
 * El caso importante es que una toma que ya existía (Room devuelve -1 al
 * insertar con IGNORE) debe recuperar su ID real para poder programar su alarma.
 */
class IntakeRepositoryImplTest {

    private lateinit var intakeDao: IntakeDao
    private lateinit var scheduleDao: ScheduleDao
    private lateinit var medicationDao: MedicationDao
    private lateinit var repository: IntakeRepositoryImpl

    private val date = LocalDate.of(2026, 10, 23)
    private val time = LocalTime.of(9, 0)
    private val scheduledAt = LocalDateTime.of(date, time)

    @Before
    fun setUp() {
        intakeDao = mockk()
        scheduleDao = mockk()
        medicationDao = mockk()
        repository = IntakeRepositoryImpl(
            intakeDao = intakeDao,
            scheduleDao = scheduleDao,
            medicationDao = medicationDao,
            sessionManager = mockk<SessionManager>(relaxed = true)
        )

        every { scheduleDao.getSchedulesByDay(WeekDay.FRIDAY) } returns flowOf(
            listOf(
                ScheduleEntity(
                    id = 10,
                    medicationId = 20,
                    weekDay = WeekDay.FRIDAY,
                    time = time
                )
            )
        )
        every { medicationDao.getMedicationById(20) } returns flowOf(
            MedicationEntity(
                id = 20,
                profileId = 1,
                name = "Ibuprofeno",
                dose = 600.0,
                unit = "mg",
                startDate = date.minusDays(1)
            )
        )
    }

    @Test
    fun `createIntakes devuelve el ID real de una toma ya existente`() = runTest {
        coEvery { intakeDao.insertIntakes(any()) } returns listOf(-1L)
        coEvery { intakeDao.getIntakesBetween(any(), any()) } returns listOf(
            IntakeEntity(
                id = 99,
                medicationId = 20,
                scheduleId = 10,
                scheduledAt = scheduledAt,
                medicationNameSnapshot = "Ibuprofeno",
                doseSnapshot = 600.0,
                unitSnapshot = "mg"
            )
        )

        val result = repository.createIntakes(date, date)

        assertEquals(1, result.size)
        assertEquals(99, result.single().id)
        assertEquals(scheduledAt, result.single().scheduledAt)
        assertEquals(20, result.single().medicationId)
        assertEquals("Ibuprofeno", result.single().medicationNameSnapshot)
        assertEquals(600.0, result.single().doseSnapshot, 0.0)
        assertEquals("mg", result.single().unitSnapshot)
    }

    @Test
    fun `createIntakes copia los datos del medicamento en los snapshots`() = runTest {
        coEvery { intakeDao.insertIntakes(any()) } returns listOf(7L)
        coEvery { intakeDao.getIntakesBetween(any(), any()) } returns listOf(
            IntakeEntity(
                id = 7,
                medicationId = 20,
                scheduleId = 10,
                scheduledAt = scheduledAt,
                medicationNameSnapshot = "Ibuprofeno",
                doseSnapshot = 600.0,
                unitSnapshot = "mg"
            )
        )

        val result = repository.createIntakes(date, date)

        assertEquals("Ibuprofeno", result.single().medicationNameSnapshot)
        assertEquals(600.0, result.single().doseSnapshot, 0.0)
        assertEquals("mg", result.single().unitSnapshot)
    }

    @Test
    fun `createIntakes conserva el ID de una toma nueva`() = runTest {
        coEvery { intakeDao.insertIntakes(any()) } returns listOf(7L)
        coEvery { intakeDao.getIntakesBetween(any(), any()) } returns listOf(
            IntakeEntity(
                id = 7,
                medicationId = 20,
                scheduleId = 10,
                scheduledAt = scheduledAt,
                medicationNameSnapshot = "Ibuprofeno",
                doseSnapshot = 600.0,
                unitSnapshot = "mg"
            )
        )

        val result = repository.createIntakes(date, date)

        assertEquals(7, result.single().id)
    }

    @Test
    fun `createIntakes no genera tomas para un medicamento archivado`() = runTest {
        every { medicationDao.getMedicationById(20) } returns flowOf(
            MedicationEntity(
                id = 20,
                profileId = 1,
                name = "Ibuprofeno",
                dose = 600.0,
                unit = "mg",
                startDate = date.minusDays(1),
                deletedAt = LocalDateTime.now()
            )
        )

        val result = repository.createIntakes(date, date)

        assertEquals(0, result.size)
    }

    @Test
    fun `getIntakeById devuelve null si la toma ya no existe`() = runTest {
        coEvery { intakeDao.getIntakeById(99) } returns null

        assertNull(repository.getIntakeById(99))
    }

    @Test
    fun `markTaken actualiza solo el estado de la toma`() = runTest {
        coEvery { intakeDao.markTaken(99, null, any()) } returns 1

        repository.markTaken(99, null)

        coVerify(exactly = 1) { intakeDao.markTaken(99, null, any()) }
    }
}
