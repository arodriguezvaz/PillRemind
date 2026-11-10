package com.andres.pillremind.data.repository

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import com.andres.pillremind.data.local.database.AppDatabase
import com.andres.pillremind.data.local.entity.ProfileEntity
import com.andres.pillremind.data.local.entity.UserEntity
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.model.MedicationWithSchedules
import com.andres.pillremind.domain.model.Schedule
import com.andres.pillremind.domain.model.ScheduleRule
import com.andres.pillremind.domain.model.ScheduleType
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Prueba de la inserción masiva usada por la importación desde PDF.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MedicationRepositoryBulkInsertTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: MedicationRepositoryImpl
    private var profileId: Int = 0

    @Before
    fun setUp() {
        val context: Context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MedicationRepositoryImpl(
            database = database,
            medicationDao = database.medicationDao(),
            scheduleDao = database.scheduleDao(),
            intakeDao = database.intakeDao()
        )
        runBlocking {
            val userId = database.userDao().insertUser(UserEntity()).toInt()
            profileId = database.profileDao()
                .insertProfile(ProfileEntity(usuarioId = userId, name = "Perfil de prueba"))
                .toInt()
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `inserta varios medicamentos y sus horarios en una transaccion`() = runTest {
        val today = LocalDate.of(2026, 11, 10)
        val ids = repository.insertMedicationsWithSchedules(
            listOf(
                MedicationWithSchedules(
                    medication = Medication(
                        profileId = profileId,
                        name = "Enalapril",
                        dose = 5.0,
                        unit = "mg",
                        startDate = today
                    ),
                    schedules = listOf(
                        Schedule(medicationId = 0, rule = ScheduleRule.EveryNDays(1), time = LocalTime.of(9, 0)),
                        Schedule(medicationId = 0, rule = ScheduleRule.EveryNDays(1), time = LocalTime.of(21, 0))
                    )
                ),
                MedicationWithSchedules(
                    medication = Medication(
                        profileId = profileId,
                        name = "Ibuprofeno",
                        dose = 600.0,
                        unit = "mg",
                        startDate = today
                    ),
                    schedules = listOf(
                        Schedule(medicationId = 0, rule = ScheduleRule.EveryNDays(1), time = LocalTime.of(9, 0))
                    )
                )
            )
        )

        assertEquals(2, ids.size)
        assertTrue(ids.all { it > 0 })

        val first = database.medicationDao().getMedicationByIdOnce(ids[0].toInt())
        val second = database.medicationDao().getMedicationByIdOnce(ids[1].toInt())
        assertEquals("Enalapril", first?.name)
        assertEquals("Ibuprofeno", second?.name)
        assertEquals(2, database.scheduleDao().getSchedules(ids[0].toInt()).first().size)
        assertEquals(1, database.scheduleDao().getSchedules(ids[1].toInt()).first().size)
        assertEquals(
            listOf(LocalTime.of(9, 0), LocalTime.of(21, 0)),
            database.scheduleDao().getSchedules(ids[0].toInt()).first()
                .map { it.time }
                .sorted()
        )
    }

    @Test
    fun `persiste la regla de cada n dias y la hora`() = runTest {
        val id = repository.insertMedicationsWithSchedules(
            listOf(
                MedicationWithSchedules(
                    medication = Medication(
                        profileId = profileId,
                        name = "Metotrexato",
                        dose = 2.5,
                        unit = "mg",
                        startDate = LocalDate.of(2026, 11, 10)
                    ),
                    schedules = listOf(
                        Schedule(
                            medicationId = 0,
                            rule = ScheduleRule.EveryNDays(3),
                            time = LocalTime.of(21, 0)
                        )
                    )
                )
            )
        ).single()

        val schedule = database.scheduleDao().getSchedules(id.toInt()).first().single()
        assertEquals(ScheduleType.EVERY_N_DAYS, schedule.type)
        assertEquals(3, schedule.intervalDays)
        assertEquals(LocalTime.of(21, 0), schedule.time)
    }

    @Test
    fun `revierte la transaccion si falla un medicamento posterior`() {
        val today = LocalDate.of(2026, 11, 10)
        val valid = MedicationWithSchedules(
            medication = Medication(
                profileId = profileId,
                name = "Enalapril",
                dose = 5.0,
                unit = "mg",
                startDate = today
            ),
            schedules = listOf(
                Schedule(
                    medicationId = 0,
                    rule = ScheduleRule.EveryNDays(1),
                    time = LocalTime.of(9, 0)
                )
            )
        )
        val invalidProfile = MedicationWithSchedules(
            medication = Medication(
                profileId = profileId + 999,
                name = "Ibuprofeno",
                dose = 600.0,
                unit = "mg",
                startDate = today
            ),
            schedules = listOf(
                Schedule(
                    medicationId = 0,
                    rule = ScheduleRule.EveryNDays(1),
                    time = LocalTime.of(9, 0)
                )
            )
        )

        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                repository.insertMedicationsWithSchedules(listOf(valid, invalidProfile))
            }
        }

        runBlocking {
            assertTrue(
                "El primer medicamento debe revertirse con la transacción",
                database.medicationDao().getAllMedications(profileId).first().isEmpty()
            )
            assertEquals(
                "Los horarios del primer medicamento también deben revertirse",
                0,
                scheduleCount()
            )
        }
    }

    @Test
    fun `revierte la transaccion si falla el horario de un medicamento`() {
        val today = LocalDate.of(2026, 11, 10)
        database.openHelper.writableDatabase.execSQL(
            """
            CREATE TRIGGER fail_schedule
            BEFORE INSERT ON schedules
            WHEN NEW.time = '21:00'
            BEGIN
                SELECT RAISE(ABORT, 'fallo simulado de horario');
            END
            """.trimIndent()
        )
        val bundle = MedicationWithSchedules(
            medication = Medication(
                profileId = profileId,
                name = "Enalapril",
                dose = 5.0,
                unit = "mg",
                startDate = today
            ),
            schedules = listOf(
                Schedule(
                    medicationId = 0,
                    rule = ScheduleRule.EveryNDays(1),
                    time = LocalTime.of(9, 0)
                ),
                Schedule(
                    medicationId = 0,
                    rule = ScheduleRule.EveryNDays(1),
                    time = LocalTime.of(21, 0)
                )
            )
        )

        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                repository.insertMedicationsWithSchedules(listOf(bundle))
            }
        }

        runBlocking {
            assertTrue(
                "El medicamento no debe quedar insertado a medias",
                database.medicationDao().getAllMedications(profileId).first().isEmpty()
            )
            assertEquals(
                "El horario insertado antes del fallo también debe revertirse",
                0,
                scheduleCount()
            )
        }
    }

    private fun scheduleCount(): Int =
        database.query("SELECT COUNT(*) FROM schedules", null).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
}
