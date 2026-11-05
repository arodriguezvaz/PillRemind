package com.andres.pillremind.data.repository

import com.andres.pillremind.data.local.dao.IntakeDao
import com.andres.pillremind.data.local.dao.MedicationDao
import com.andres.pillremind.data.local.dao.ScheduleDao
import com.andres.pillremind.data.local.database.AppDatabase
import com.andres.pillremind.data.local.entity.MedicationEntity
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas de las consultas de medicamentos usadas por estadísticas.
 */
class MedicationRepositoryImplTest {

    private lateinit var medicationDao: MedicationDao
    private lateinit var repository: MedicationRepositoryImpl

    @Before
    fun setUp() {
        medicationDao = mockk()
        repository = MedicationRepositoryImpl(
            database = mockk<AppDatabase>(),
            medicationDao = medicationDao,
            scheduleDao = mockk<ScheduleDao>(),
            intakeDao = mockk<IntakeDao>()
        )
    }

    @Test
    fun `hasMedications es falso cuando el perfil no tiene medicamentos`() = runTest {
        every { medicationDao.countMedications(1) } returns flowOf(0)

        assertFalse(repository.hasMedications(1).first())
    }

    @Test
    fun `hasMedications es verdadero aunque solo haya medicamentos archivados`() = runTest {
        every { medicationDao.countMedications(1) } returns flowOf(2)

        assertTrue(repository.hasMedications(1).first())
    }

    @Test
    fun `getAllMedications incluye los medicamentos archivados`() = runTest {
        every { medicationDao.getAllMedications(1) } returns flowOf(
            listOf(
                MedicationEntity(
                    id = 2,
                    profileId = 1,
                    name = "Omeprazol",
                    dose = 20.0,
                    unit = "mg",
                    startDate = LocalDate.of(2026, 1, 1),
                    deletedAt = LocalDateTime.of(2026, 10, 1, 20, 0),
                )
            )
        )

        val result = repository.getAllMedications(1).first()

        assertEquals(1, result.size)
        assertEquals("Omeprazol", result.single().name)
        assertNotNull(result.single().deletedAt)
    }
}
