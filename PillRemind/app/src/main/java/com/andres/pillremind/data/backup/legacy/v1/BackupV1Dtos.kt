package com.andres.pillremind.data.backup.legacy.v1

import kotlinx.serialization.Serializable

/**
 * Copia de seguridad con el formato 1.
 *
 * Se conserva congelado para poder importar archivos creados por versiones
 * internas anteriores a la versión 2. No debe modificarse cuando cambie el
 * formato actual.
 */
@Serializable
internal data class BackupV1FileDto(
    val format: String,
    val formatVersion: Int,
    val appVersion: String,
    val exportedAt: String,
    val users: List<BackupV1UserDto>,
    val profiles: List<BackupV1ProfileDto>,
    val medications: List<BackupV1MedicationDto>,
    val schedules: List<BackupV1ScheduleDto>,
    val intakes: List<BackupV1IntakeDto>,
)

@Serializable
internal data class BackupV1UserDto(
    val id: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
internal data class BackupV1ProfileDto(
    val id: Int,
    val usuarioId: Int,
    val name: String,
    val imagePath: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
internal data class BackupV1MedicationDto(
    val id: Int,
    val profileId: Int,
    val name: String,
    val dose: Double,
    val unit: String,
    val isChronic: Boolean,
    val startDate: String,
    val endDate: String? = null,
    val imagePath: String? = null,
    val leafletUrl: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null,
)

@Serializable
internal data class BackupV1ScheduleDto(
    val id: Int,
    val medicationId: Int,
    val weekDay: String,
    val time: String,
)

@Serializable
internal data class BackupV1IntakeDto(
    val id: Int,
    val medicationId: Int,
    val scheduleId: Int? = null,
    val scheduledAt: String,
    val takenAt: String? = null,
    val medicationNameSnapshot: String,
    val doseSnapshot: Double,
    val unitSnapshot: String,
    val createdAt: String,
    val updatedAt: String,
)
