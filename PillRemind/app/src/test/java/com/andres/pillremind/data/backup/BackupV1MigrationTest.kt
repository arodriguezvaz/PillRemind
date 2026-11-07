package com.andres.pillremind.data.backup

import com.andres.pillremind.data.backup.legacy.v1.BackupV1FileDto
import com.andres.pillremind.data.backup.legacy.v1.BackupV1IntakeDto
import com.andres.pillremind.data.backup.legacy.v1.BackupV1MedicationDto
import com.andres.pillremind.data.backup.legacy.v1.BackupV1ProfileDto
import com.andres.pillremind.data.backup.legacy.v1.BackupV1ScheduleDto
import com.andres.pillremind.data.backup.legacy.v1.BackupV1UserDto
import com.andres.pillremind.domain.model.ScheduleType
import com.andres.pillremind.domain.model.WeekDay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Pruebas de compatibilidad con copias del formato 1.
 */
class BackupV1MigrationTest {

    private val json = Json { encodeDefaults = true }

    @Test
    fun `migra una copia v1 a la representacion actual`() {
        val v1 = validV1Backup()

        val migrated = BackupSerializer.decode(json.encodeToString(v1))

        assertEquals(BackupFormat.VERSION, migrated.formatVersion)
        assertNull(migrated.profiles.single().birthDate)
        assertNull(migrated.profiles.single().sex)
        assertNull(migrated.profiles.single().bloodType)
        assertNull(migrated.profiles.single().allergies)
        assertNull(migrated.profiles.single().medicalNotes)
        assertNull(migrated.profiles.single().healthCardNumber)

        val schedule = migrated.schedules.single()
        assertEquals(ScheduleType.WEEKLY.name, schedule.type)
        assertEquals(WeekDay.MONDAY.name, schedule.weekDay)
        assertEquals("08:00", schedule.time)

        BackupValidator.validate(migrated)

        val entities = migrated.toEntities(emptyMap())
        assertEquals(1, entities.schedules.size)
        assertEquals(ScheduleType.WEEKLY, entities.schedules.single().type)
        assertEquals(WeekDay.MONDAY, entities.schedules.single().weekDay)
    }

    @Test
    fun `rechaza una version desconocida`() {
        val exception = assertThrows(BackupException::class.java) {
            BackupSerializer.decode(
                """{"format":"${BackupFormat.NAME}","formatVersion":99}"""
            )
        }

        assertEquals(BackupError.UNSUPPORTED_VERSION, exception.error)
    }

    @Test
    fun `rechaza un formato desconocido`() {
        val exception = assertThrows(BackupException::class.java) {
            BackupSerializer.decode(
                """{"format":"otro-formato","formatVersion":2}"""
            )
        }

        assertEquals(BackupError.INVALID_FILE, exception.error)
    }

    private fun validV1Backup(): BackupV1FileDto = BackupV1FileDto(
        format = BackupFormat.NAME,
        formatVersion = 1,
        appVersion = "1.0.0",
        exportedAt = "2026-10-31T12:00:00",
        users = listOf(
            BackupV1UserDto(
                id = 1,
                createdAt = "2026-10-01T09:00:00",
                updatedAt = "2026-10-01T09:00:00",
            )
        ),
        profiles = listOf(
            BackupV1ProfileDto(
                id = 1,
                usuarioId = 1,
                name = "Ana",
                createdAt = "2026-10-01T09:00:00",
                updatedAt = "2026-10-01T09:00:00",
            )
        ),
        medications = listOf(
            BackupV1MedicationDto(
                id = 1,
                profileId = 1,
                name = "Ibuprofeno",
                dose = 400.0,
                unit = "mg",
                isChronic = false,
                startDate = "2026-10-01",
                createdAt = "2026-10-01T09:00:00",
                updatedAt = "2026-10-01T09:00:00",
            )
        ),
        schedules = listOf(
            BackupV1ScheduleDto(
                id = 1,
                medicationId = 1,
                weekDay = WeekDay.MONDAY.name,
                time = "08:00",
            )
        ),
        intakes = listOf(
            BackupV1IntakeDto(
                id = 1,
                medicationId = 1,
                scheduleId = 1,
                scheduledAt = "2026-10-31T08:00:00",
                medicationNameSnapshot = "Ibuprofeno",
                doseSnapshot = 400.0,
                unitSnapshot = "mg",
                createdAt = "2026-10-31T08:00:00",
                updatedAt = "2026-10-31T08:00:00",
            )
        ),
    )
}
