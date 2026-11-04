package com.andres.pillremind.data.repository

import android.content.Context
import androidx.room.Room
import com.andres.pillremind.data.backup.BackupException
import com.andres.pillremind.data.backup.BackupImageStore
import com.andres.pillremind.data.local.database.AppDatabase
import com.andres.pillremind.data.local.entity.IntakeEntity
import com.andres.pillremind.data.local.entity.MedicationEntity
import com.andres.pillremind.data.local.entity.ProfileEntity
import com.andres.pillremind.data.local.entity.ScheduleEntity
import com.andres.pillremind.data.local.entity.UserEntity
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.ScheduleType
import com.andres.pillremind.domain.model.WeekDay
import com.andres.pillremind.notifications.ReminderNotifications
import com.andres.pillremind.notifications.ReminderScheduler
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Pruebas de integración de la exportación e importación con una base de datos
 * Room en memoria.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BackupRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var reminderScheduler: ReminderScheduler
    private lateinit var repository: BackupRepositoryImpl

    private val now = LocalDateTime.of(2026, 10, 31, 12, 0)

    @Before
    fun setUp() = runBlocking {
        context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sessionManager = SessionManager(context)
        sessionManager.clearActiveProfile()
        sessionManager.clearLastGeneratedDate()
        reminderScheduler = mockk(relaxed = true)
        coEvery { reminderScheduler.generateWindow(any(), any()) } returns emptyList()
        every { reminderScheduler.cancelAlarms(any()) } just Runs
        repository = BackupRepositoryImpl(
            database = database,
            backupDao = database.backupDao(),
            imageStore = BackupImageStore(context),
            sessionManager = sessionManager,
            reminderScheduler = reminderScheduler,
            notifications = ReminderNotifications(context),
            context = context
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `exporta e importa perfiles medicamentos historial e imagenes`() = runTest {
        val seeded = seedDatabase()
        val exported = ByteArrayOutputStream()

        repository.exportTo(exported, password = null)

        // Se modifica la base de datos para comprobar que la importación la reemplaza.
        val currentProfile = database.backupDao().getProfilesOnce().first()
        database.profileDao().updateProfile(currentProfile.copy(name = "Nombre cambiado"))
        sessionManager.setActiveProfile(seeded.profileId)

        repository.importFrom(
            input = ByteArrayInputStream(exported.toByteArray()),
            password = null
        )

        val profiles = database.backupDao().getProfilesOnce()
        val medications = database.backupDao().getMedicationsOnce()
        val schedules = database.backupDao().getSchedulesOnce()
        val intakes = database.backupDao().getIntakesOnce()

        assertEquals(1, profiles.size)
        assertEquals("Ana", profiles.first().name)
        assertEquals(1, medications.size)
        assertEquals("Ibuprofeno", medications.first().name)
        assertEquals(1, schedules.size)
        assertEquals(LocalTime.of(8, 0), schedules.first().time)
        assertEquals(1, intakes.size)

        val restoredImage = File(profiles.first().imageUrl!!)
        assertTrue(restoredImage.exists())
        assertEquals(listOf<Byte>(1, 2, 3, 4), restoredImage.readBytes().toList())
        assertFalse(File(seeded.imagePath).exists())

        assertNull(sessionManager.activeProfileId.first())
        assertEquals(seeded.userId, sessionManager.activeUserId.first())
        assertNull(sessionManager.lastGeneratedDate.first())
        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }
        verify(exactly = 1) { reminderScheduler.cancelAlarms(any()) }
    }

    @Test
    fun `exporta e importa con contrasena`() = runTest {
        seedDatabase()
        val password = "clave-segura-123".toCharArray()
        val exported = ByteArrayOutputStream()

        repository.exportTo(exported, password = password)

        repository.importFrom(
            input = ByteArrayInputStream(exported.toByteArray()),
            password = "clave-segura-123".toCharArray()
        )

        assertEquals("Ana", database.backupDao().getProfilesOnce().first().name)
    }

    @Test
    fun `una contrasena incorrecta no modifica los datos`() = runTest {
        seedDatabase()
        val password = "clave-segura-123".toCharArray()
        val exported = ByteArrayOutputStream()
        repository.exportTo(exported, password = password)

        val currentProfile = database.backupDao().getProfilesOnce().first()
        database.profileDao().updateProfile(currentProfile.copy(name = "Sin tocar"))
        sessionManager.setActiveProfile(currentProfile.id)

        val result = runCatching {
            repository.importFrom(
                input = ByteArrayInputStream(exported.toByteArray()),
                password = "otra-contrasena".toCharArray()
            )
        }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BackupException)
        assertEquals("Sin tocar", database.backupDao().getProfilesOnce().first().name)
        assertEquals(currentProfile.id, sessionManager.activeProfileId.first())
    }

    @Test
    fun `exporta e importa horarios recurrentes`() = runTest {
        val seeded = seedDatabase()
        database.scheduleDao().insertSchedule(
            ScheduleEntity(
                medicationId = seeded.medicationId,
                type = ScheduleType.EVERY_N_DAYS,
                intervalDays = 2,
                time = LocalTime.of(20, 0)
            )
        )
        val exported = ByteArrayOutputStream()

        repository.exportTo(exported, password = null)

        repository.importFrom(
            input = ByteArrayInputStream(exported.toByteArray()),
            password = null
        )

        val schedules = database.backupDao().getSchedulesOnce()
        assertEquals(2, schedules.size)
        val recurring = schedules.first { it.type == ScheduleType.EVERY_N_DAYS }
        assertEquals(2, recurring.intervalDays)
        assertEquals(LocalTime.of(20, 0), recurring.time)
    }

    private suspend fun seedDatabase(): SeededData {
        val userId = database.userDao()
            .insertUser(UserEntity(createdAt = now, updatedAt = now))
            .toInt()
        val imageFile = File(context.filesDir, "profile_test.jpg").apply {
            writeBytes(byteArrayOf(1, 2, 3, 4))
        }
        val profileId = database.profileDao().insertProfile(
            ProfileEntity(
                usuarioId = userId,
                name = "Ana",
                imageUrl = imageFile.absolutePath,
                createdAt = now,
                updatedAt = now
            )
        ).toInt()
        val medicationId = database.medicationDao().insertMedication(
            MedicationEntity(
                profileId = profileId,
                name = "Ibuprofeno",
                dose = 400.0,
                unit = "mg",
                startDate = LocalDate.of(2026, 10, 1),
                createdAt = now,
                updatedAt = now
            )
        ).toInt()
        val scheduleId = database.scheduleDao().insertSchedule(
            ScheduleEntity(
                medicationId = medicationId,
                weekDay = WeekDay.MONDAY,
                time = LocalTime.of(8, 0)
            )
        ).toInt()
        database.intakeDao().insertIntakes(
            listOf(
                IntakeEntity(
                    medicationId = medicationId,
                    scheduleId = scheduleId,
                    scheduledAt = now.plusDays(1),
                    medicationNameSnapshot = "Ibuprofeno",
                    doseSnapshot = 400.0,
                    unitSnapshot = "mg",
                    createdAt = now,
                    updatedAt = now
                )
            )
        )

        return SeededData(
            userId = userId,
            profileId = profileId,
            medicationId = medicationId,
            imagePath = imageFile.absolutePath
        )
    }

    private data class SeededData(
        val userId: Int,
        val profileId: Int,
        val medicationId: Int,
        val imagePath: String
    )
}
