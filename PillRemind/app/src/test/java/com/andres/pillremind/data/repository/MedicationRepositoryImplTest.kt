package com.andres.pillremind.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.andres.pillremind.data.local.database.AppDatabase
import com.andres.pillremind.data.local.entity.IntakeEntity
import com.andres.pillremind.data.local.entity.ProfileEntity
import com.andres.pillremind.data.local.entity.UserEntity
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.model.Schedule
import com.andres.pillremind.domain.model.ScheduleRule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas del ciclo de archivo, restauración y borrado definitivo.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MedicationRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: MedicationRepositoryImpl

    private val now = LocalDateTime.of(2026, 10, 31, 12, 0)
    private var profileId = 0

    @Before
    fun setUp() = runBlocking {
        val context: Context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MedicationRepositoryImpl(
            database = database,
            medicationDao = database.medicationDao(),
            scheduleDao = database.scheduleDao(),
            intakeDao = database.intakeDao(),
        )

        val userId = database.userDao()
            .insertUser(UserEntity(createdAt = now, updatedAt = now))
            .toInt()
        profileId = database.profileDao()
            .insertProfile(
                ProfileEntity(
                    usuarioId = userId,
                    name = "Ana",
                    createdAt = now,
                    updatedAt = now,
                )
            )
            .toInt()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `archivar conserva la pauta y elimina solo las tomas futuras`() = runTest {
        val medicationId = seedMedication()
        val pastIntakeId = insertIntake(medicationId, now.minusDays(1))
        val futureIntakeId = insertIntake(medicationId, now.plusDays(1))

        repository.softDeleteMedication(medicationId, now, now)

        val archived = database.medicationDao().getMedicationByIdOnce(medicationId)
        assertNotNull(archived?.deletedAt)
        assertEquals(
            listOf(LocalTime.of(8, 0)),
            database.scheduleDao().getSchedules(medicationId).first().map { it.time },
        )
        assertNotNull(database.intakeDao().getIntakeById(pastIntakeId))
        assertNull(database.intakeDao().getIntakeById(futureIntakeId))
    }

    @Test
    fun `restaurar recupera el medicamento y conserva su pauta`() = runTest {
        val medicationId = seedMedication()
        repository.softDeleteMedication(medicationId, now, now)

        repository.restoreMedication(medicationId, now.plusHours(1))

        assertNull(database.medicationDao().getMedicationByIdOnce(medicationId)?.deletedAt)
        assertEquals(1, database.scheduleDao().getSchedules(medicationId).first().size)
    }

    @Test
    fun `borrar definitivamente elimina medicamento, pauta y tomas`() = runTest {
        val medicationId = seedMedication()
        insertIntake(medicationId, now.minusDays(1))
        insertIntake(medicationId, now.plusDays(1))

        repository.deleteMedicationPermanently(medicationId)

        assertNull(database.medicationDao().getMedicationByIdOnce(medicationId))
        assertEquals(0, database.scheduleDao().getSchedules(medicationId).first().size)
        assertEquals(
            0,
            database.intakeDao()
                .getIntakesBetween(now.minusYears(1), now.plusYears(1))
                .size,
        )
    }

    @Test
    fun `rechaza guardar una dosis no positiva`() = runTest {
        val medication = Medication(
            profileId = profileId,
            name = "Ibuprofeno",
            dose = 0.0,
            unit = "mg",
            startDate = LocalDate.of(2026, 10, 1),
        )

        val result = runCatching {
            repository.insertMedicationWithSchedules(medication, emptyList())
        }

        assertEquals(true, result.isFailure)
        assertEquals(0, repository.getMedicationsOnce(profileId).size)
    }

    private suspend fun seedMedication(): Int {
        val medication = Medication(
            profileId = profileId,
            name = "Ibuprofeno",
            dose = 600.0,
            unit = "mg",
            startDate = LocalDate.of(2026, 10, 1),
        )
        return repository.insertMedicationWithSchedules(
            medication = medication,
            schedules = listOf(
                Schedule(
                    medicationId = 0,
                    rule = ScheduleRule.Weekly(com.andres.pillremind.domain.model.WeekDay.MONDAY),
                    time = LocalTime.of(8, 0),
                )
            ),
        ).toInt()
    }

    private suspend fun insertIntake(medicationId: Int, scheduledAt: LocalDateTime): Int {
        return database.intakeDao().insertIntakes(
            listOf(
                IntakeEntity(
                    medicationId = medicationId,
                    scheduledAt = scheduledAt,
                    medicationNameSnapshot = "Ibuprofeno",
                    doseSnapshot = 600.0,
                    unitSnapshot = "mg",
                    createdAt = now,
                    updatedAt = now,
                )
            )
        ).first().toInt()
    }
}
