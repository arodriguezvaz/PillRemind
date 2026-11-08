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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas instrumentadas de las consultas usadas para localizar las tomas cuyas
 * notificaciones se cancelan al archivar, editar o borrar un medicamento, al
 * borrar un perfil y al importar una copia de seguridad.
 *
 * Verifican el JOIN contra `medications`, el aislamiento entre perfiles y que
 * las tomas de medicamentos archivados también se incluyen, porque Room las
 * elimina en cascada al borrar el perfil.
 */
@RunWith(AndroidJUnit4::class)
class IntakeDaoNotificationIdsTest {

    private lateinit var database: AppDatabase
    private lateinit var intakeDao: IntakeDao

    private val day = LocalDate.of(2026, 11, 5)

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
    fun getIntakeIdsByMedicationIdSoloDevuelveLasTomasDeEseMedicamento() = runBlocking {
        val profileId = insertProfile("Ana")
        val ibuprofeno = insertMedication(profileId, "Ibuprofeno")
        val omeprazol = insertMedication(profileId, "Omeprazol")
        val pendingId = insertIntake(ibuprofeno, day.atTime(8, 0))
        val takenId = insertIntake(
            ibuprofeno,
            day.atTime(20, 0),
            takenAt = day.atTime(20, 5)
        )
        insertIntake(omeprazol, day.atTime(9, 0))

        val ids = intakeDao.getIntakeIdsByMedicationId(ibuprofeno)

        assertEquals(setOf(pendingId, takenId), ids.toSet())
    }

    @Test
    fun getIntakeIdsByProfileIdIncluyeArchivadosYExcluyeOtrosPerfiles() = runBlocking {
        val ana = insertProfile("Ana")
        val luis = insertProfile("Luis")
        val activeMedication = insertMedication(ana, "Enalapril")
        val archivedMedication = insertMedication(
            ana,
            "Ibuprofeno",
            deletedAt = day.atTime(12, 0)
        )
        val otherProfileMedication = insertMedication(luis, "Omeprazol")
        val activeIntakeId = insertIntake(activeMedication, day.atTime(8, 0))
        val archivedIntakeId = insertIntake(archivedMedication, day.atTime(9, 0))
        insertIntake(otherProfileMedication, day.atTime(10, 0))

        val ids = intakeDao.getIntakeIdsByProfileId(ana)

        assertEquals(setOf(activeIntakeId, archivedIntakeId), ids.toSet())
    }

    private suspend fun insertProfile(name: String): Int {
        val userId = database.userDao().insertUser(UserEntity()).toInt()
        return database.profileDao().insertProfile(
            ProfileEntity(
                usuarioId = userId,
                name = name
            )
        ).toInt()
    }

    private suspend fun insertMedication(
        profileId: Int,
        name: String,
        deletedAt: LocalDateTime? = null
    ): Int =
        database.medicationDao().insertMedication(
            MedicationEntity(
                profileId = profileId,
                name = name,
                dose = 600.0,
                unit = "mg",
                startDate = day.minusDays(30),
                deletedAt = deletedAt
            )
        ).toInt()

    private suspend fun insertIntake(
        medicationId: Int,
        scheduledAt: LocalDateTime,
        takenAt: LocalDateTime? = null
    ): Int =
        intakeDao.insertIntakes(
            listOf(
                IntakeEntity(
                    medicationId = medicationId,
                    scheduledAt = scheduledAt,
                    takenAt = takenAt,
                    medicationNameSnapshot = "Medicamento",
                    doseSnapshot = 600.0,
                    unitSnapshot = "mg"
                )
            )
        ).single().toInt()
}
