package com.andres.pillremind.data.repository

import com.andres.pillremind.data.local.dao.IntakeDao
import com.andres.pillremind.data.local.dao.MedicationDao
import com.andres.pillremind.data.local.dao.ScheduleDao
import com.andres.pillremind.data.local.database.AppDatabase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
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
}
