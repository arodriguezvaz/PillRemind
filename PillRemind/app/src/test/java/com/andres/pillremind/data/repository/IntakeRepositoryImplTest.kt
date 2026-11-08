package com.andres.pillremind.data.repository

import com.andres.pillremind.data.local.dao.IntakeDao
import com.andres.pillremind.data.local.dao.MedicationDao
import com.andres.pillremind.data.local.dao.ScheduleDao
import com.andres.pillremind.data.local.entity.IntakeEntity
import com.andres.pillremind.data.local.entity.MedicationEntity
import com.andres.pillremind.data.local.entity.ScheduleEntity
import com.andres.pillremind.data.local.projections.IntakeWithMedicationRow
import com.andres.pillremind.data.local.projections.StatisticsIntakeRow
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.ScheduleType
import com.andres.pillremind.domain.model.WeekDay
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun `createIntakes genera tomas cada dos dias`() = runTest {
        coEvery { scheduleDao.getAllSchedulesOnce() } returns listOf(
            ScheduleEntity(
                id = 10,
                medicationId = 20,
                type = ScheduleType.EVERY_N_DAYS,
                intervalDays = 2,
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
                startDate = date.minusDays(2)
            )
        )
        coEvery { intakeDao.insertIntakes(any()) } returns listOf(1L, 2L)

        val result = repository.createIntakes(date, date.plusDays(3))

        assertEquals(2, result.size)
        assertEquals(
            listOf(date, date.plusDays(2)),
            result.map { it.scheduledAt.toLocalDate() }
        )
    }

    @Test
    fun `createIntakes genera tomas el dia del mes`() = runTest {
        coEvery { scheduleDao.getAllSchedulesOnce() } returns listOf(
            ScheduleEntity(
                id = 10,
                medicationId = 20,
                type = ScheduleType.MONTHLY_DAY,
                dayOfMonth = date.dayOfMonth,
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
                startDate = date.minusMonths(1)
            )
        )
        coEvery { intakeDao.insertIntakes(any()) } returns listOf(1L)

        val result = repository.createIntakes(date, date.plusDays(2))

        assertEquals(1, result.size)
        assertEquals(date, result.single().scheduledAt.toLocalDate())
    }

    @Test
    fun `createIntakes respeta los ciclos de toma y descanso`() = runTest {
        coEvery { scheduleDao.getAllSchedulesOnce() } returns listOf(
            ScheduleEntity(
                id = 10,
                medicationId = 20,
                type = ScheduleType.CYCLE,
                onDays = 2,
                offDays = 1,
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
                startDate = date
            )
        )
        coEvery { intakeDao.insertIntakes(any()) } returns listOf(1L, 2L, 3L, 4L)

        val result = repository.createIntakes(date, date.plusDays(4))

        assertEquals(4, result.size)
        assertEquals(
            listOf(date, date.plusDays(1), date.plusDays(3), date.plusDays(4)),
            result.map { it.scheduledAt.toLocalDate() }
        )
    }

    @Test
    fun `createIntakes genera una sola toma`() = runTest {
        coEvery { scheduleDao.getAllSchedulesOnce() } returns listOf(
            ScheduleEntity(
                id = 10,
                medicationId = 20,
                type = ScheduleType.SINGLE,
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
                startDate = date
            )
        )
        coEvery { intakeDao.insertIntakes(any()) } returns listOf(1L)

        val result = repository.createIntakes(date, date.plusDays(2))

        assertEquals(1, result.size)
        assertEquals(date, result.single().scheduledAt.toLocalDate())
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
    fun `observeIntakesForDay mapea la proyeccion de Room al dominio`() = runTest {
        val startOfDay = date.atStartOfDay()
        val endOfDay = date.atTime(LocalTime.MAX)
        every { intakeDao.observeIntakesForDay(startOfDay, endOfDay, 1) } returns flowOf(
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

        val result = repository.observeIntakesForDay(1, date).first()

        assertEquals(1, result.size)
        assertEquals(99, result.single().intakeId)
        assertEquals(20, result.single().medicationId)
        assertEquals("Ibuprofeno", result.single().medicationName)
        assertEquals("Andres", result.single().profileName)
        verify(exactly = 1) {
            intakeDao.observeIntakesForDay(startOfDay, endOfDay, 1)
        }
    }

    @Test
    fun `observeStatisticsIntakes mapea la proyeccion y conserva los archivados`() = runTest {
        val from = date.atStartOfDay()
        val to = date.atTime(LocalTime.MAX)
        every {
            intakeDao.observeStatisticsIntakes(1, from, to)
        } returns flowOf(
            listOf(
                StatisticsIntakeRow(
                    intakeId = 99,
                    medicationId = 20,
                    scheduledAt = scheduledAt,
                    takenAt = scheduledAt.plusMinutes(5),
                    medicationName = "Ibuprofeno",
                    dose = 600.0,
                    unit = "mg",
                    imageUrl = null,
                    deletedAt = LocalDateTime.of(2026, 10, 23, 20, 0)
                )
            )
        )

        val result = repository.observeStatisticsIntakes(1, from, to).first()

        assertEquals(1, result.size)
        assertEquals(99, result.single().intakeId)
        assertEquals("Ibuprofeno", result.single().medicationName)
        assertEquals(600.0, result.single().dose, 0.0)
        assertEquals(scheduledAt.plusMinutes(5), result.single().takenAt)
        assertTrue(result.single().isArchived)
    }

    @Test
    fun `observeStatisticsIntakes consulta el rango indicado`() = runTest {
        val from = date.atStartOfDay()
        val to = date.atTime(LocalTime.MAX)
        every { intakeDao.observeStatisticsIntakes(1, from, to) } returns flowOf(emptyList())

        repository.observeStatisticsIntakes(1, from, to).first()

        verify(exactly = 1) { intakeDao.observeStatisticsIntakes(1, from, to) }
    }

    @Test
    fun `observeHistoryIntakes mapea la proyeccion y conserva los archivados`() = runTest {
        val from = date.atStartOfDay()
        val to = date.atTime(LocalTime.MAX)
        every {
            intakeDao.observeHistoryIntakes(1, from, to)
        } returns flowOf(
            listOf(
                StatisticsIntakeRow(
                    intakeId = 99,
                    medicationId = 20,
                    scheduledAt = scheduledAt,
                    takenAt = scheduledAt.plusMinutes(5),
                    medicationName = "Ibuprofeno",
                    dose = 600.0,
                    unit = "mg",
                    imageUrl = null,
                    deletedAt = LocalDateTime.of(2026, 10, 23, 20, 0)
                )
            )
        )

        val result = repository.observeHistoryIntakes(1, from, to).first()

        assertEquals(1, result.size)
        assertEquals(99, result.single().intakeId)
        assertEquals("Ibuprofeno", result.single().medicationName)
        assertEquals(600.0, result.single().dose, 0.0)
        assertEquals(scheduledAt.plusMinutes(5), result.single().takenAt)
        assertTrue(result.single().isArchived)
    }

    @Test
    fun `observeHistoryIntakes admite un rango abierto`() = runTest {
        val to = date.atTime(LocalTime.MAX)
        every {
            intakeDao.observeHistoryIntakes(1, null, to)
        } returns flowOf(emptyList())

        repository.observeHistoryIntakes(1, null, to).first()

        verify(exactly = 1) { intakeDao.observeHistoryIntakes(1, null, to) }
    }

    @Test
    fun `getIntakeIdsByMedicationId delega en el DAO`() = runTest {
        coEvery { intakeDao.getIntakeIdsByMedicationId(20) } returns listOf(1, 2, 3)

        assertEquals(listOf(1, 2, 3), repository.getIntakeIdsByMedicationId(20))
    }

    @Test
    fun `getIntakeIdsByProfileId delega en el DAO`() = runTest {
        coEvery { intakeDao.getIntakeIdsByProfileId(1) } returns listOf(4, 5)

        assertEquals(listOf(4, 5), repository.getIntakeIdsByProfileId(1))
    }
}
