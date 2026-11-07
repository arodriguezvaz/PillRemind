package com.andres.pillremind.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.andres.pillremind.data.local.database.AppDatabase
import com.andres.pillremind.data.local.entity.IntakeEntity
import com.andres.pillremind.data.local.entity.MedicationEntity
import com.andres.pillremind.data.local.entity.ProfileEntity
import com.andres.pillremind.data.local.entity.UserEntity
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas instrumentadas de [IntakeDao] contra una base de datos Room real.
 *
 * Verifican que marcar y deshacer una toma solo modifica `takenAt` y
 * `updatedAt`, sin tocar los snapshots que conserva el historial.
 */
@RunWith(AndroidJUnit4::class)
class IntakeDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var intakeDao: IntakeDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        intakeDao = database.intakeDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun deshacerUnaTomaConservaLosSnapshotsYActualizaLaFecha() = runBlocking {
        val intakeId = insertarToma(takenAt = LocalDateTime.of(2026, 11, 5, 8, 5))
        val updatedAt = LocalDateTime.of(2026, 11, 5, 9, 0)

        assertEquals(1, intakeDao.markTaken(intakeId, null, updatedAt))

        val intake = intakeDao.getIntakeById(intakeId)
        assertNotNull(intake)
        assertNull(intake?.takenAt)
        assertEquals("Ibuprofeno", intake?.medicationNameSnapshot)
        assertEquals(600.0, intake?.doseSnapshot ?: 0.0, 0.0)
        assertEquals("mg", intake?.unitSnapshot)
        assertEquals(updatedAt, intake?.updatedAt)
    }

    @Test
    fun marcarUnaTomaRegistraLaHoraIndicada() = runBlocking {
        val intakeId = insertarToma(takenAt = null)
        val takenAt = LocalDateTime.of(2026, 11, 5, 8, 10)
        val updatedAt = LocalDateTime.of(2026, 11, 5, 8, 11)

        assertEquals(1, intakeDao.markTaken(intakeId, takenAt, updatedAt))

        val intake = intakeDao.getIntakeById(intakeId)
        assertNotNull(intake)
        assertEquals(takenAt, intake?.takenAt)
        assertEquals(updatedAt, intake?.updatedAt)
    }

    private suspend fun insertarToma(takenAt: LocalDateTime?): Int {
        val userId = database.userDao().insertUser(UserEntity()).toInt()
        val profileId = database.profileDao().insertProfile(
            ProfileEntity(
                usuarioId = userId,
                name = "Ana"
            )
        ).toInt()
        val medicationId = database.medicationDao().insertMedication(
            MedicationEntity(
                profileId = profileId,
                name = "Ibuprofeno",
                dose = 600.0,
                unit = "mg",
                startDate = LocalDate.of(2026, 1, 1)
            )
        ).toInt()

        return intakeDao.insertIntakes(
            listOf(
                IntakeEntity(
                    medicationId = medicationId,
                    scheduledAt = LocalDateTime.of(2026, 11, 5, 8, 0),
                    takenAt = takenAt,
                    medicationNameSnapshot = "Ibuprofeno",
                    doseSnapshot = 600.0,
                    unitSnapshot = "mg"
                )
            )
        ).single().toInt()
    }
}
