package com.andres.pillremind.data.repository

import com.andres.pillremind.data.local.dao.IntakeDao
import com.andres.pillremind.data.local.dao.MedicationDao
import com.andres.pillremind.data.local.dao.ScheduleDao
import com.andres.pillremind.data.local.entity.IntakeEntity
import com.andres.pillremind.data.local.entity.MedicationEntity
import com.andres.pillremind.data.local.entity.ScheduleEntity
import com.andres.pillremind.data.local.projections.IntakeWithMedicationRow
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.WeekDay
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
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
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: IntakeRepositoryImpl

    private val date = LocalDate.of(2026, 10, 23)
    private val nextDay = date.plusDays(1)
    private val time = LocalTime.of(9, 0)
    private val scheduledAt = LocalDateTime.of(date, time)

    @Before
    fun setUp() {
        intakeDao = mockk()
        scheduleDao = mockk()
        medicationDao = mockk()
        sessionManager = mockk(relaxed = true)
        repository = IntakeRepositoryImpl(
            intakeDao = intakeDao,
            scheduleDao = scheduleDao,
            medicationDao = medicationDao,
            sessionManager = sessionManager
        )

        coEvery { scheduleDao.getAllSchedulesOnce() } returns listOf(
            ScheduleEntity(
                id = 10,
                medicationId = 20,
                weekDay = WeekDay.FRIDAY,
                time = time
            )
        )
        coEvery { medicationDao.getMedicationsByIdsOnce(any()) } returns listOf(
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

        val result = repository.createIntakes(date, date)

        assertEquals("Ibuprofeno", result.single().medicationNameSnapshot)
        assertEquals(600.0, result.single().doseSnapshot, 0.0)
        assertEquals("mg", result.single().unitSnapshot)
    }

    @Test
    fun `createIntakes conserva el ID de una toma nueva`() = runTest {
        coEvery { intakeDao.insertIntakes(any()) } returns listOf(7L)

        val result = repository.createIntakes(date, date)

        assertEquals(7, result.single().id)
        coVerify(exactly = 0) { intakeDao.getIntakesBetween(any(), any()) }
    }

    @Test
    fun `createIntakes consulta horarios y medicamentos una sola vez`() = runTest {
        coEvery { scheduleDao.getAllSchedulesOnce() } returns listOf(
            ScheduleEntity(
                id = 10,
                medicationId = 20,
                weekDay = WeekDay.FRIDAY,
                time = time
            ),
            ScheduleEntity(
                id = 11,
                medicationId = 20,
                weekDay = WeekDay.SATURDAY,
                time = time
            )
        )
        coEvery { intakeDao.insertIntakes(any()) } returns listOf(1L, 2L)

        val result = repository.createIntakes(date, nextDay)

        assertEquals(2, result.size)
        coVerify(exactly = 1) { scheduleDao.getAllSchedulesOnce() }
        coVerify(exactly = 1) { medicationDao.getMedicationsByIdsOnce(listOf(20)) }
        coVerify(exactly = 0) { intakeDao.getIntakesBetween(any(), any()) }
    }

    @Test
    fun `createIntakes no consulta medicamentos si no hay horarios`() = runTest {
        coEvery { scheduleDao.getAllSchedulesOnce() } returns emptyList()

        val result = repository.createIntakes(date, date)

        assertEquals(0, result.size)
        coVerify(exactly = 0) { medicationDao.getMedicationsByIdsOnce(any()) }
        coVerify(exactly = 0) { intakeDao.insertIntakes(any()) }
    }

    @Test
    fun `createIntakes no genera tomas para un medicamento archivado`() = runTest {
        coEvery { medicationDao.getMedicationsByIdsOnce(any()) } returns listOf(
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
    fun `createIntakes no genera tomas antes de la fecha de inicio`() = runTest {
        coEvery { medicationDao.getMedicationsByIdsOnce(any()) } returns listOf(
            MedicationEntity(
                id = 20,
                profileId = 1,
                name = "Ibuprofeno",
                dose = 600.0,
                unit = "mg",
                startDate = date.plusDays(1)
            )
        )

        val result = repository.createIntakes(date, date)

        assertEquals(0, result.size)
    }

    @Test
    fun `createIntakes no genera tomas despues de la fecha de fin`() = runTest {
        coEvery { medicationDao.getMedicationsByIdsOnce(any()) } returns listOf(
            MedicationEntity(
                id = 20,
                profileId = 1,
                name = "Ibuprofeno",
                dose = 600.0,
                unit = "mg",
                startDate = date.minusDays(1),
                endDate = date.minusDays(1)
            )
        )

        val result = repository.createIntakes(date, date)

        assertEquals(0, result.size)
    }

    @Test
    fun `createIntakes no genera tomas de otro dia de la semana`() = runTest {
        coEvery { scheduleDao.getAllSchedulesOnce() } returns listOf(
            ScheduleEntity(
                id = 10,
                medicationId = 20,
                weekDay = WeekDay.MONDAY,
                time = time
            )
        )

        val result = repository.createIntakes(date, date)

        assertEquals(0, result.size)
    }

    @Test
    fun `getLastGeneratedDate devuelve hoy si no hay fecha guardada`() = runTest {
        every { sessionManager.lastGeneratedDate } returns flowOf(null)

        assertEquals(LocalDate.now(), repository.getLastGeneratedDate())
    }

    @Test
    fun `getLastGeneratedDate devuelve la fecha guardada`() = runTest {
        every { sessionManager.lastGeneratedDate } returns flowOf(date)

        assertEquals(date, repository.getLastGeneratedDate())
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

    @Test
    fun `getIntakesForToday mapea la proyeccion de Room al dominio`() = runTest {
        every { intakeDao.getIntakesForToday(any(), any(), any()) } returns flowOf(
            listOf(
                IntakeWithMedicationRow(
                    intakeId = 99,
                    medicationId = 20,
                    scheduledAt = scheduledAt,
                    scheduleId = 10,
                    takenAt = null,
                    medicationName = "Ibuprofeno",
                    dose = 600.0,
                    unit = "mg",
                    imageUrl = null,
                    profileName = "Andres"
                )
            )
        )

        val result = repository.getIntakesForToday(date, 1).first()

        assertEquals(1, result.size)
        assertEquals(99, result.single().intakeId)
        assertEquals(20, result.single().medicationId)
        assertEquals("Ibuprofeno", result.single().medicationName)
        assertEquals("Andres", result.single().profileName)
    }
}
