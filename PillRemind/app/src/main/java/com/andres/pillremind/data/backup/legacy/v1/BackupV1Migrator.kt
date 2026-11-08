package com.andres.pillremind.data.backup.legacy.v1

import com.andres.pillremind.data.backup.BackupFileDto
import com.andres.pillremind.data.backup.BackupFormat
import com.andres.pillremind.data.backup.BackupIntakeDto
import com.andres.pillremind.data.backup.BackupMedicationDto
import com.andres.pillremind.data.backup.BackupProfileDto
import com.andres.pillremind.data.backup.BackupScheduleDto
import com.andres.pillremind.data.backup.BackupUserDto
import com.andres.pillremind.domain.model.ScheduleType

/**
 * Convierte una copia del formato 1 a la representación actual.
 *
 * El formato 1 solo admitía horarios semanales y no tenía los datos médicos del
 * perfil. Los campos nuevos se rellenan con valores válidos por defecto:
 * horario `WEEKLY` y datos médicos `null`.
 */
internal fun BackupV1FileDto.toCurrentBackup(): BackupFileDto = BackupFileDto(
    format = format,
    formatVersion = BackupFormat.VERSION,
    appVersion = appVersion,
    exportedAt = exportedAt,
    users = users.map { it.toCurrent() },
    profiles = profiles.map { it.toCurrent() },
    medications = medications.map { it.toCurrent() },
    schedules = schedules.map { it.toCurrent() },
    intakes = intakes.map { it.toCurrent() },
)

private fun BackupV1UserDto.toCurrent() = BackupUserDto(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun BackupV1ProfileDto.toCurrent() = BackupProfileDto(
    id = id,
    usuarioId = usuarioId,
    name = name,
    imagePath = imagePath,
    birthDate = null,
    sex = null,
    bloodType = null,
    allergies = null,
    medicalNotes = null,
    healthCardNumber = null,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun BackupV1MedicationDto.toCurrent() = BackupMedicationDto(
    id = id,
    profileId = profileId,
    name = name,
    dose = dose,
    unit = unit,
    isChronic = isChronic,
    startDate = startDate,
    endDate = endDate,
    imagePath = imagePath,
    leafletUrl = leafletUrl,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
)

private fun BackupV1ScheduleDto.toCurrent() = BackupScheduleDto(
    id = id,
    medicationId = medicationId,
    type = ScheduleType.WEEKLY.name,
    weekDay = weekDay,
    time = time,
)

private fun BackupV1IntakeDto.toCurrent() = BackupIntakeDto(
    id = id,
    medicationId = medicationId,
    scheduleId = scheduleId,
    scheduledAt = scheduledAt,
    takenAt = takenAt,
    medicationNameSnapshot = medicationNameSnapshot,
    doseSnapshot = doseSnapshot,
    unitSnapshot = unitSnapshot,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
