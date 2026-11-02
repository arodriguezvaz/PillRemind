package com.andres.pillremind.data.backup

import com.andres.pillremind.domain.model.BloodType
import com.andres.pillremind.domain.model.ScheduleType
import com.andres.pillremind.domain.model.Sex
import com.andres.pillremind.domain.model.WeekDay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Pruebas de serialización, validación y mapeo de la copia de seguridad.
 */
class BackupSerializerTest {

    @Test
    fun `ida y vuelta conserva todos los datos`() {
        val original = validBackup()

        val restored = BackupSerializer.decode(BackupSerializer.encode(original))

        assertEquals(original, restored)
    }

    @Test
    fun `ida y vuelta conserva horarios recurrentes`() {
        val original = validBackup().copy(
            schedules = listOf(
                BackupScheduleDto(
                    id = 1,
                    medicationId = 1,
                    type = ScheduleType.EVERY_N_DAYS.name,
                    intervalDays = 2,
                    time = "08:00"
                ),
                BackupScheduleDto(
                    id = 2,
                    medicationId = 1,
                    type = ScheduleType.MONTHLY_WEEKDAY.name,
                    weekDay = WeekDay.MONDAY.name,
                    monthWeek = -1,
                    time = "09:00"
                )
            )
        )

        val restored = BackupSerializer.decode(BackupSerializer.encode(original))

        assertEquals(original, restored)
    }

    @Test
    fun `rechaza una copia del formato anterior`() {
        val backup = validBackup().copy(formatVersion = 1)

        assertThrows(BackupException::class.java) { BackupValidator.validate(backup) }
    }

    @Test
    fun `rechaza un formato desconocido`() {
        val backup = validBackup().copy(format = "otro-formato")

        assertThrows(BackupException::class.java) { BackupValidator.validate(backup) }
    }

    @Test
    fun `rechaza una version no soportada`() {
        val backup = validBackup().copy(formatVersion = 99)

        assertThrows(BackupException::class.java) { BackupValidator.validate(backup) }
    }

    @Test
    fun `rechaza un perfil que referencia un usuario inexistente`() {
        val backup = validBackup().copy(
            profiles = listOf(validBackup().profiles.first().copy(usuarioId = 99))
        )

        assertThrows(BackupException::class.java) { BackupValidator.validate(backup) }
    }

    @Test
    fun `rechaza una fecha invalida`() {
        val backup = validBackup().copy(
            users = listOf(validBackup().users.first().copy(createdAt = "no-es-una-fecha"))
        )

        assertThrows(BackupException::class.java) { BackupValidator.validate(backup) }
    }

    @Test
    fun `rechaza una ruta de imagen fuera de images`() {
        val backup = validBackup().copy(
            profiles = listOf(validBackup().profiles.first().copy(imagePath = "../evil.jpg"))
        )

        assertThrows(BackupException::class.java) { BackupValidator.validate(backup) }
    }

    @Test
    fun `rechaza tomas duplicadas`() {
        val backup = validBackup()
        val duplicated = backup.intakes.first().copy(id = 2)
        val result = backup.copy(intakes = backup.intakes + duplicated)

        assertThrows(BackupException::class.java) { BackupValidator.validate(result) }
    }

    @Test
    fun `mapea el contenido a entidades de Room`() {
        val backup = validBackup()
        val entities = backup.toEntities(
            mapOf("images/img_0001.jpg" to "/data/app/files/backup_1.jpg")
        )

        assertEquals(1, entities.users.size)
        assertEquals(1, entities.profiles.size)
        assertEquals(2, entities.medications.size)
        assertEquals(1, entities.schedules.size)
        assertEquals(2, entities.intakes.size)

        assertEquals("/data/app/files/backup_1.jpg", entities.profiles.first().imageUrl)
        assertEquals(LocalDate.of(1990, 5, 12), entities.profiles.first().birthDate)
        assertEquals(Sex.FEMALE, entities.profiles.first().sex)
        assertEquals(BloodType.O_POSITIVE, entities.profiles.first().bloodType)
        assertEquals("Penicilina", entities.profiles.first().allergies)
        assertEquals("Asma", entities.profiles.first().medicalNotes)
        assertEquals("SIP-123", entities.profiles.first().healthCardNumber)
        assertEquals("/data/app/files/backup_1.jpg", entities.medications.first().imageUrl)
        assertEquals(WeekDay.MONDAY, entities.schedules.first().weekDay)
        assertEquals(ScheduleType.WEEKLY, entities.schedules.first().type)
        assertEquals(LocalTime.of(8, 0), entities.schedules.first().time)
        assertEquals(LocalDate.of(2026, 10, 1), entities.medications.first().startDate)
        assertNull(entities.intakes.first().takenAt)
        assertEquals(
            LocalDateTime.of(2026, 10, 31, 8, 0),
            entities.intakes.first().scheduledAt
        )

        val archived = entities.medications[1]
        assertEquals(LocalDateTime.of(2026, 10, 20, 10, 0), archived.deletedAt)
    }

    @Test
    fun `mapea horarios recurrentes`() {
        val backup = validBackup().copy(
            schedules = listOf(
                BackupScheduleDto(
                    id = 1,
                    medicationId = 1,
                    type = ScheduleType.EVERY_N_DAYS.name,
                    intervalDays = 2,
                    time = "08:00"
                ),
                BackupScheduleDto(
                    id = 2,
                    medicationId = 1,
                    type = ScheduleType.MONTHLY_DAY.name,
                    dayOfMonth = 31,
                    clampToLastDay = true,
                    time = "09:00"
                )
            )
        )

        BackupValidator.validate(backup)
        val entities = backup.toEntities(
            mapOf("images/img_0001.jpg" to "/data/app/files/backup_1.jpg")
        )

        assertEquals(ScheduleType.EVERY_N_DAYS, entities.schedules[0].type)
        assertEquals(2, entities.schedules[0].intervalDays)
        assertEquals(ScheduleType.MONTHLY_DAY, entities.schedules[1].type)
        assertEquals(31, entities.schedules[1].dayOfMonth)
        assertEquals(true, entities.schedules[1].clampToLastDay)
    }

    @Test
    fun `rechaza un horario recurrente sin sus datos`() {
        val backup = validBackup().copy(
            schedules = listOf(
                BackupScheduleDto(
                    id = 1,
                    medicationId = 1,
                    type = ScheduleType.EVERY_N_DAYS.name,
                    time = "08:00"
                )
            )
        )

        assertThrows(BackupException::class.java) { BackupValidator.validate(backup) }
    }

    @Test
    fun `acepta una copia antigua sin los datos medicos del perfil`() {
        val backup = validBackup().copy(
            profiles = listOf(
                validBackup().profiles.first().copy(
                    birthDate = null,
                    sex = null,
                    bloodType = null,
                    allergies = null,
                    medicalNotes = null,
                    healthCardNumber = null
                )
            )
        )

        BackupValidator.validate(backup)
        val entities = backup.toEntities(
            mapOf("images/img_0001.jpg" to "/data/app/files/backup_1.jpg")
        )

        assertNull(entities.profiles.first().birthDate)
        assertNull(entities.profiles.first().sex)
        assertNull(entities.profiles.first().bloodType)
        assertNull(entities.profiles.first().allergies)
        assertNull(entities.profiles.first().medicalNotes)
        assertNull(entities.profiles.first().healthCardNumber)
    }

    @Test
    fun `rechaza una fecha de nacimiento invalida`() {
        val backup = validBackup().copy(
            profiles = listOf(validBackup().profiles.first().copy(birthDate = "no-es-una-fecha"))
        )

        assertThrows(BackupException::class.java) { BackupValidator.validate(backup) }
    }

    @Test
    fun `rechaza un sexo invalido`() {
        val backup = validBackup().copy(
            profiles = listOf(validBackup().profiles.first().copy(sex = "NO_EXISTE"))
        )

        assertThrows(BackupException::class.java) { BackupValidator.validate(backup) }
    }

    @Test
    fun `rechaza un grupo sanguineo invalido`() {
        val backup = validBackup().copy(
            profiles = listOf(validBackup().profiles.first().copy(bloodType = "NO_EXISTE"))
        )

        assertThrows(BackupException::class.java) { BackupValidator.validate(backup) }
    }

    private fun validBackup(): BackupFileDto = BackupFileDto(
        format = BackupFormat.NAME,
        formatVersion = BackupFormat.VERSION,
        appVersion = "1.0",
        exportedAt = "2026-10-31T12:00:00",
        users = listOf(
            BackupUserDto(
                id = 1,
                createdAt = "2026-10-01T09:00:00",
                updatedAt = "2026-10-01T09:00:00"
            )
        ),
        profiles = listOf(
            BackupProfileDto(
                id = 1,
                usuarioId = 1,
                name = "Ana",
                imagePath = "images/img_0001.jpg",
                birthDate = "1990-05-12",
                sex = Sex.FEMALE.name,
                bloodType = BloodType.O_POSITIVE.name,
                allergies = "Penicilina",
                medicalNotes = "Asma",
                healthCardNumber = "SIP-123",
                createdAt = "2026-10-01T09:00:00",
                updatedAt = "2026-10-01T09:00:00"
            )
        ),
        medications = listOf(
            BackupMedicationDto(
                id = 1,
                profileId = 1,
                name = "Ibuprofeno",
                dose = 400.0,
                unit = "mg",
                isChronic = false,
                startDate = "2026-10-01",
                imagePath = "images/img_0001.jpg",
                createdAt = "2026-10-01T09:00:00",
                updatedAt = "2026-10-01T09:00:00"
            ),
            BackupMedicationDto(
                id = 2,
                profileId = 1,
                name = "Omeprazol",
                dose = 20.0,
                unit = "mg",
                isChronic = true,
                startDate = "2026-10-02",
                createdAt = "2026-10-02T09:00:00",
                updatedAt = "2026-10-20T10:00:00",
                deletedAt = "2026-10-20T10:00:00"
            )
        ),
        schedules = listOf(
            BackupScheduleDto(
                id = 1,
                medicationId = 1,
                type = ScheduleType.WEEKLY.name,
                weekDay = WeekDay.MONDAY.name,
                time = "08:00"
            )
        ),
        intakes = listOf(
            BackupIntakeDto(
                id = 1,
                medicationId = 1,
                scheduleId = 1,
                scheduledAt = "2026-10-31T08:00:00",
                medicationNameSnapshot = "Ibuprofeno",
                doseSnapshot = 400.0,
                unitSnapshot = "mg",
                createdAt = "2026-10-31T08:00:00",
                updatedAt = "2026-10-31T08:00:00"
            ),
            BackupIntakeDto(
                id = 2,
                medicationId = 2,
                scheduleId = null,
                scheduledAt = "2026-10-31T09:00:00",
                takenAt = "2026-10-31T09:05:00",
                medicationNameSnapshot = "Omeprazol",
                doseSnapshot = 20.0,
                unitSnapshot = "mg",
                createdAt = "2026-10-31T09:00:00",
                updatedAt = "2026-10-31T09:05:00"
            )
        )
    )
}
