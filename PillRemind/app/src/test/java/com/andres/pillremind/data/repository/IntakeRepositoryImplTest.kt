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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
            IntakeEntity(id = 99, scheduleId = 10, scheduledAt = scheduledAt)
        )

        val result = repository.createIntakes(date, date)

        assertEquals(1, result.size)
        assertEquals(99, result.single().id)
        assertEquals(scheduledAt, result.single().scheduledAt)
    }

    @Test
    fun `createIntakes conserva el ID de una toma nueva`() = runTest {
        coEvery { intakeDao.insertIntakes(any()) } returns listOf(7L)
        coEvery { intakeDao.getIntakesBetween(any(), any()) } returns listOf(
            IntakeEntity(id = 7, scheduleId = 10, scheduledAt = scheduledAt)
        )

        val result = repository.createIntakes(date, date)

        assertEquals(7, result.single().id)
    }
}
