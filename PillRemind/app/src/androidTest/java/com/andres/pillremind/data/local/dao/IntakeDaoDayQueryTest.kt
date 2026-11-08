package com.andres.pillremind.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.andres.pillremind.data.local.database.AppDatabase
import com.andres.pillremind.data.local.entity.IntakeEntity
import com.andres.pillremind.data.local.entity.MedicationEntity
import com.andres.pillremind.data.local.entity.ProfileEntity
import com.andres.pillremind.data.local.entity.UserEntity
import com.andres.pillremind.data.local.projections.IntakeWithMedicationRow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas instrumentadas de la consulta de tomas de un día concreto.
 *
 * Comprueban la ventana de fechas que usa Inicio para cargar hoy y ayer, el
 * caso de la toma de las 23:00 y el aislamiento entre perfiles.
 */
@RunWith(AndroidJUnit4::class)
class IntakeDaoDayQueryTest {

    private lateinit var database: AppDatabase
    private lateinit var intakeDao: IntakeDao

    private val day = LocalDate.of(2026, 11, 5)
    private val previousDay = day.minusDays(1)
    private val nextDay = day.plusDays(1)

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
    fun elDiaIncluyeLasTomasDeLaNocheYExcluyeLaMedianocheSiguiente() = runBlocking {
        val profileId = insertProfile("Ana")
        val lateIntakeId = insertIntake(profileId, day.atTime(23, 0))
        val lastMinuteIntakeId = insertIntake(profileId, day.atTime(23, 59))
        insertIntake(profileId, nextDay.atStartOfDay())
        insertIntake(profileId, previousDay.atTime(23, 0))

        val result = observeDay(profileId, day)

        assertEquals(
            listOf(lateIntakeId, lastMinuteIntakeId),
            result.map { it.intakeId }
        )
        assertEquals(
            listOf(day.atTime(23, 0), day.atTime(23, 59)),
            result.map { it.scheduledAt }
        )
    }

    @Test
    fun laConsultaDevuelveLaTomaPendienteDeLas23ConSuEstado() = runBlocking {
        val profileId = insertProfile("Ana")
        val intakeId = insertIntake(profileId, day.atTime(23, 0))

        val result = observeDay(profileId, day)

        assertEquals(listOf(intakeId), result.map { it.intakeId })
        assertNull(result.single().takenAt)
    }

    @Test
    fun laConsultaNoMezclaTomasDeOtrosPerfiles() = runBlocking {
        val ana = insertProfile("Ana")
        val luis = insertProfile("Luis")
        insertIntake(ana, day.atTime(9, 0))
        insertIntake(luis, day.atTime(10, 0))

        val result = observeDay(ana, day)

        assertEquals(listOf("Ana"), result.map { it.profileName })
        assertEquals(listOf(day.atTime(9, 0)), result.map { it.scheduledAt })
    }

    private suspend fun observeDay(
        profileId: Int,
        date: LocalDate
    ): List<IntakeWithMedicationRow> =
        intakeDao.observeIntakesForDay(
            startOfDay = date.atStartOfDay(),
            endOfDay = date.atTime(LocalTime.MAX),
            profileId = profileId
        ).first()

    private suspend fun insertProfile(name: String): Int {
        val userId = database.userDao().insertUser(UserEntity()).toInt()
        return database.profileDao().insertProfile(
            ProfileEntity(
                usuarioId = userId,
                name = name
            )
        ).toInt()
    }

    private suspend fun insertIntake(
        profileId: Int,
        scheduledAt: LocalDateTime
    ): Int {
        val medicationId = database.medicationDao().insertMedication(
            MedicationEntity(
                profileId = profileId,
                name = "Ibuprofeno",
                dose = 600.0,
                unit = "mg",
                startDate = previousDay
            )
        ).toInt()
        return intakeDao.insertIntakes(
            listOf(
                IntakeEntity(
                    medicationId = medicationId,
                    scheduledAt = scheduledAt,
                    medicationNameSnapshot = "Ibuprofeno",
                    doseSnapshot = 600.0,
                    unitSnapshot = "mg"
                )
            )
        ).single().toInt()
    }
}
