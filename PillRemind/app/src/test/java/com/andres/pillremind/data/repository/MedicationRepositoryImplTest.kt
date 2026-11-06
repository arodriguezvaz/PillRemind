package com.andres.pillremind.data.repository

import com.andres.pillremind.data.local.dao.IntakeDao
import com.andres.pillremind.data.local.dao.MedicationDao
import com.andres.pillremind.data.local.dao.ScheduleDao
import com.andres.pillremind.data.local.database.AppDatabase
import com.andres.pillremind.data.local.entity.MedicationEntity
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

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
    fun `getArchivedMedications mapea las entidades a dominio`() = runTest {
        val archivedAt = LocalDateTime.of(2026, 11, 2, 18, 30)
        every { medicationDao.getArchivedMedications(1) } returns flowOf(
            listOf(
                MedicationEntity(
                    id = 3,
                    profileId = 1,
                    name = "Enalapril",
                    dose = 10.0,
                    unit = "mg",
                    startDate = LocalDate.of(2026, 3, 12),
                    deletedAt = archivedAt,
                )
            )
        )

        val archived = repository.getArchivedMedications(1).first()

        assertEquals(1, archived.size)
        assertEquals(3, archived.first().id)
        assertEquals(archivedAt, archived.first().deletedAt)
    }

    @Test
    fun `unarchiveMedication limpia el archivo con la fecha indicada`() = runTest {
        val updatedAt = LocalDateTime.of(2026, 11, 5, 9, 15)
        coEvery { medicationDao.unarchiveMedication(3, updatedAt) } just Runs

        repository.unarchiveMedication(3, updatedAt)

        coVerify { medicationDao.unarchiveMedication(3, updatedAt) }
    }
}
