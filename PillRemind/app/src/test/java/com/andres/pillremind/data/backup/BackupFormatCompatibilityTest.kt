package com.andres.pillremind.data.backup

import com.andres.pillremind.domain.model.ScheduleType
import com.andres.pillremind.domain.model.WeekDay
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Pruebas de compatibilidad con los formatos de copia antiguos.
 *
 * El fixture v1 es un JSON literal, no se construye con el DTO congelado: así
 * el test falla si el contrato histórico deja de coincidir con lo que la app
 * antigua exportaba.
 */
class BackupFormatCompatibilityTest {

    @Test
    fun `migra una copia v1 historica completa`() {
        val migrated = BackupSerializer.decode(goldenV1Json())

        BackupValidator.validate(migrated)

        assertEquals(BackupFormat.VERSION, migrated.formatVersion)
        assertEquals(1, migrated.users.size)
        assertEquals("2026-10-01T09:00:00", migrated.users.single().createdAt)

        val profile = migrated.profiles.single()
        assertEquals("Ana v1", profile.name)
        assertEquals("images/profile.jpg", profile.imagePath)
        assertNull(profile.birthDate)
        assertNull(profile.sex)
        assertNull(profile.bloodType)
        assertNull(profile.allergies)
        assertNull(profile.medicalNotes)
        assertNull(profile.healthCardNumber)

        val medications = migrated.medications.associateBy { it.id }
        val ibuprofen = medications.getValue(1)
        assertEquals("Ibuprofeno v1", ibuprofen.name)
        assertEquals(400.0, ibuprofen.dose, 0.0)
        assertEquals("2026-10-31", ibuprofen.endDate)
        assertEquals("images/medication.png", ibuprofen.imagePath)
        assertEquals("https://example.com/prospecto", ibuprofen.leafletUrl)
        assertEquals("2026-10-20T10:00:00", medications.getValue(2).deletedAt)

        val schedule = migrated.schedules.single()
        assertEquals(ScheduleType.WEEKLY.name, schedule.type)
        assertEquals(WeekDay.MONDAY.name, schedule.weekDay)
        assertEquals("08:00", schedule.time)

        val intakes = migrated.intakes.associateBy { it.id }
        assertEquals(1, intakes.getValue(1).scheduleId)
        assertEquals("2026-10-31T08:05:00", intakes.getValue(1).takenAt)
        assertNull(intakes.getValue(2).scheduleId)
        assertNull(intakes.getValue(2).takenAt)

        val entities = migrated.toEntities(
            mapOf(
                "images/profile.jpg" to "/tmp/profile.jpg",
                "images/medication.png" to "/tmp/medication.png",
            )
        )
        assertEquals(2, entities.medications.size)
        assertEquals(2, entities.intakes.size)
        assertEquals(ScheduleType.WEEKLY, entities.schedules.single().type)
        assertEquals(WeekDay.MONDAY, entities.schedules.single().weekDay)
        assertEquals(
            LocalDateTime.of(2026, 10, 31, 8, 5),
            entities.intakes.first { it.id == 1 }.takenAt,
        )
    }

    @Test
    fun `migra una copia v1 sin campos opcionales`() {
        val migrated = BackupSerializer.decode(MINIMAL_V1)

        BackupValidator.validate(migrated)

        assertEquals(BackupFormat.VERSION, migrated.formatVersion)
        assertNull(migrated.profiles.single().imagePath)
        assertNull(migrated.medications.single().endDate)
        assertNull(migrated.medications.single().imagePath)
        assertNull(migrated.medications.single().leafletUrl)
        assertNull(migrated.medications.single().deletedAt)
        assertNull(migrated.intakes.single().scheduleId)
        assertNull(migrated.intakes.single().takenAt)
    }

    @Test
    fun `lee una copia v2 anterior a los datos medicos del perfil`() {
        val migrated = BackupSerializer.decode(OLD_V2)

        BackupValidator.validate(migrated)

        assertEquals(BackupFormat.VERSION, migrated.formatVersion)
        val profile = migrated.profiles.single()
        assertEquals("Ana", profile.name)
        assertNull(profile.birthDate)
        assertNull(profile.sex)
        assertNull(profile.bloodType)
        assertNull(profile.allergies)
        assertNull(profile.medicalNotes)
        assertNull(profile.healthCardNumber)
    }

    @Test
    fun `rechaza un json malformado`() {
        assertInvalidFile("{")
    }

    @Test
    fun `rechaza un json vacio`() {
        assertInvalidFile("")
    }

    @Test
    fun `rechaza un json que no es un objeto`() {
        assertInvalidFile("[]")
    }

    @Test
    fun `rechaza un envelope sin format`() {
        assertInvalidFile("""{"formatVersion":2}""")
    }

    @Test
    fun `rechaza un envelope sin formatVersion`() {
        assertInvalidFile("""{"format":"${BackupFormat.NAME}"}""")
    }

    @Test
    fun `rechaza una version de formato no numerica`() {
        assertInvalidFile("""{"format":"${BackupFormat.NAME}","formatVersion":"dos"}""")
    }

    @Test
    fun `rechaza una version desconocida`() {
        assertUnsupportedVersion("""{"format":"${BackupFormat.NAME}","formatVersion":3}""")
        assertUnsupportedVersion("""{"format":"${BackupFormat.NAME}","formatVersion":99}""")
    }

    @Test
    fun `rechaza un formato desconocido`() {
        assertInvalidFile("""{"format":"otro-formato","formatVersion":2}""")
    }

    @Test
    fun `rechaza una copia v1 incompleta`() {
        assertInvalidFile("""{"format":"${BackupFormat.NAME}","formatVersion":1}""")
    }

    @Test
    fun `rechaza un horario v1 con un dia invalido`() {
        assertInvalidData(goldenV1Json().replace("\"MONDAY\"", "\"NO_EXISTE\""))
    }

    @Test
    fun `rechaza un horario v1 con una hora invalida`() {
        assertInvalidData(goldenV1Json().replace("\"08:00\"", "\"25:00\""))
    }

    private fun assertInvalidFile(content: String) {
        val exception = assertThrows(BackupException::class.java) {
            BackupSerializer.decode(content)
        }
        assertEquals(BackupError.INVALID_FILE, exception.error)
    }

    private fun assertUnsupportedVersion(content: String) {
        val exception = assertThrows(BackupException::class.java) {
            BackupSerializer.decode(content)
        }
        assertEquals(BackupError.UNSUPPORTED_VERSION, exception.error)
    }

    private fun assertInvalidData(content: String) {
        val migrated = BackupSerializer.decode(content)
        val exception = assertThrows(BackupException::class.java) {
            BackupValidator.validate(migrated)
        }
        assertEquals(BackupError.INVALID_DATA, exception.error)
    }

    private fun goldenV1Json(): String =
        checkNotNull(javaClass.classLoader?.getResource("backup-v1.json")) {
            "Falta el fixture backup-v1.json"
        }.readText()

    private companion object {
        val MINIMAL_V1 = """
            {
              "format": "pillremind-backup",
              "formatVersion": 1,
              "appVersion": "1.0.0",
              "exportedAt": "2026-10-31T12:00:00",
              "users": [
                {
                  "id": 1,
                  "createdAt": "2026-10-01T09:00:00",
                  "updatedAt": "2026-10-01T09:00:00"
                }
              ],
              "profiles": [
                {
                  "id": 1,
                  "usuarioId": 1,
                  "name": "Ana",
                  "createdAt": "2026-10-01T09:00:00",
                  "updatedAt": "2026-10-01T09:00:00"
                }
              ],
              "medications": [
                {
                  "id": 1,
                  "profileId": 1,
                  "name": "Ibuprofeno",
                  "dose": 400.0,
                  "unit": "mg",
                  "isChronic": false,
                  "startDate": "2026-10-01",
                  "createdAt": "2026-10-01T09:00:00",
                  "updatedAt": "2026-10-01T09:00:00"
                }
              ],
              "schedules": [
                {
                  "id": 1,
                  "medicationId": 1,
                  "weekDay": "MONDAY",
                  "time": "08:00"
                }
              ],
              "intakes": [
                {
                  "id": 1,
                  "medicationId": 1,
                  "scheduledAt": "2026-10-31T08:00:00",
                  "medicationNameSnapshot": "Ibuprofeno",
                  "doseSnapshot": 400.0,
                  "unitSnapshot": "mg",
                  "createdAt": "2026-10-31T08:00:00",
                  "updatedAt": "2026-10-31T08:00:00"
                }
              ]
            }
        """.trimIndent()

        val OLD_V2 = """
            {
              "format": "pillremind-backup",
              "formatVersion": 2,
              "appVersion": "1.1.0",
              "exportedAt": "2026-10-31T12:00:00",
              "users": [
                {
                  "id": 1,
                  "createdAt": "2026-10-01T09:00:00",
                  "updatedAt": "2026-10-01T09:00:00"
                }
              ],
              "profiles": [
                {
                  "id": 1,
                  "usuarioId": 1,
                  "name": "Ana",
                  "createdAt": "2026-10-01T09:00:00",
                  "updatedAt": "2026-10-01T09:00:00"
                }
              ],
              "medications": [],
              "schedules": [],
              "intakes": []
            }
        """.trimIndent()
    }
}
