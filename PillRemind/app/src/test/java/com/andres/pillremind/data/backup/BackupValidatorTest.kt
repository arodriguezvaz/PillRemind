package com.andres.pillremind.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Pruebas de las validaciones que se aplican antes de importar una copia.
 */
class BackupValidatorTest {

    @Test
    fun `acepta una copia coherente`() {
        BackupValidator.validate(validFile())
    }

    @Test
    fun `rechaza una dosis no positiva`() {
        val file = validFile(
            medications = listOf(medication(id = 1, dose = 0.0))
        )

        assertInvalidData { BackupValidator.validate(file) }
    }

    @Test
    fun `rechaza una fecha de fin anterior al inicio`() {
        val file = validFile(
            medications = listOf(
                medication(
                    id = 1,
                    startDate = "2026-10-10",
                    endDate = "2026-10-09",
                )
            )
        )

        assertInvalidData { BackupValidator.validate(file) }
    }

    @Test
    fun `rechaza una toma que usa el horario de otro medicamento`() {
        val file = validFile(
            medications = listOf(
                medication(id = 1),
                medication(id = 2, name = "Omeprazol"),
            ),
            schedules = listOf(
                schedule(id = 10, medicationId = 1),
                schedule(id = 11, medicationId = 2),
            ),
            intakes = listOf(
                intake(id = 1, medicationId = 1, scheduleId = 11),
            ),
        )

        assertInvalidData { BackupValidator.validate(file) }
    }

    @Test
    fun `rechaza una dosis historica negativa`() {
        val file = validFile(
            intakes = listOf(
                intake(id = 1, medicationId = 1, scheduleId = 10, doseSnapshot = -1.0)
            )
        )

        assertInvalidData { BackupValidator.validate(file) }
    }

    @Test
    fun `rechaza tomas duplicadas del mismo medicamento y minuto`() {
        val file = validFile(
            intakes = listOf(
                intake(id = 1, medicationId = 1, scheduleId = 10),
                intake(id = 2, medicationId = 1, scheduleId = 10),
            )
        )

        assertInvalidData { BackupValidator.validate(file) }
    }

    private fun assertInvalidData(block: () -> Unit) {
        val error = assertThrows(BackupException::class.java) { block() }
        assertEquals(BackupError.INVALID_DATA, error.error)
    }

    private fun validFile(
        medications: List<BackupMedicationDto> = listOf(medication(id = 1)),
        schedules: List<BackupScheduleDto> = listOf(schedule(id = 10, medicationId = 1)),
        intakes: List<BackupIntakeDto> = listOf(
            intake(id = 1, medicationId = 1, scheduleId = 10)
        ),
    ) = BackupFileDto(
        format = BackupFormat.NAME,
        formatVersion = BackupFormat.VERSION,
        appVersion = "1.0",
        exportedAt = "2026-10-31T12:00",
        users = listOf(
            BackupUserDto(
                id = 1,
                createdAt = "2026-10-01T09:00",
                updatedAt = "2026-10-01T09:00",
            )
        ),
        profiles = listOf(
            BackupProfileDto(
                id = 1,
                usuarioId = 1,
                name = "Ana",
                createdAt = "2026-10-01T09:00",
                updatedAt = "2026-10-01T09:00",
            )
        ),
        medications = medications,
        schedules = schedules,
        intakes = intakes,
    )

    private fun medication(
        id: Int,
        name: String = "Ibuprofeno",
        dose: Double = 600.0,
        startDate: String = "2026-10-01",
        endDate: String? = null,
    ) = BackupMedicationDto(
        id = id,
        profileId = 1,
        name = name,
        dose = dose,
        unit = "mg",
        isChronic = false,
        startDate = startDate,
        endDate = endDate,
        createdAt = "2026-10-01T09:00",
        updatedAt = "2026-10-01T09:00",
    )

    private fun schedule(id: Int, medicationId: Int) = BackupScheduleDto(
        id = id,
        medicationId = medicationId,
        type = "WEEKLY",
        weekDay = "MONDAY",
        time = "08:00",
    )

    private fun intake(
        id: Int,
        medicationId: Int,
        scheduleId: Int?,
        doseSnapshot: Double = 600.0,
    ) = BackupIntakeDto(
        id = id,
        medicationId = medicationId,
        scheduleId = scheduleId,
        scheduledAt = "2026-10-31T08:00",
        medicationNameSnapshot = "Ibuprofeno",
        doseSnapshot = doseSnapshot,
        unitSnapshot = "mg",
        createdAt = "2026-10-01T09:00",
        updatedAt = "2026-10-01T09:00",
    )
}
