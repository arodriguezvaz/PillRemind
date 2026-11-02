package com.andres.pillremind.data.backup

import kotlinx.serialization.Serializable

/**
 * Metadatos del formato de copia de seguridad.
 */
object BackupFormat {
    /** Identificador que debe aparecer en el archivo de datos. */
    const val NAME = "pillremind-backup"

    /** Versión del formato de datos. Se incrementa si cambia el esquema. */
    const val VERSION = 2
}

/**
 * Contenido completo de una copia de seguridad.
 *
 * Las fechas se guardan como texto ISO para no depender de los converters de
 * Room y para que el archivo sea legible con cualquier herramienta.
 *
 * @property format Identificador del formato.
 * @property formatVersion Versión del formato de datos.
 * @property appVersion Versión de la aplicación que generó la copia.
 * @property exportedAt Momento de la exportación.
 * @property users Usuarios locales.
 * @property profiles Perfiles de la aplicación.
 * @property medications Medicamentos, incluidos los archivados.
 * @property schedules Horarios asociados a los medicamentos.
 * @property intakes Historial y tomas futuras.
 */
@Serializable
data class BackupFileDto(
    val format: String,
    val formatVersion: Int,
    val appVersion: String,
    val exportedAt: String,
    val users: List<BackupUserDto>,
    val profiles: List<BackupProfileDto>,
    val medications: List<BackupMedicationDto>,
    val schedules: List<BackupScheduleDto>,
    val intakes: List<BackupIntakeDto>
)

/**
 * Usuario local propietario de los perfiles.
 */
@Serializable
data class BackupUserDto(
    val id: Int,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Perfil incluido en la copia.
 *
 * @property imagePath Ruta relativa dentro del ZIP (`images/...`), o `null`.
 * @property birthDate Fecha de nacimiento ISO, o `null`.
 * @property sex Sexo declarado, o `null`.
 * @property bloodType Grupo sanguíneo declarado, o `null`.
 * @property allergies Alergias conocidas, o `null`.
 * @property medicalNotes Condiciones o notas médicas, o `null`.
 * @property healthCardNumber Número de tarjeta sanitaria, o `null`.
 */
@Serializable
data class BackupProfileDto(
    val id: Int,
    val usuarioId: Int,
    val name: String,
    val imagePath: String? = null,
    val birthDate: String? = null,
    val sex: String? = null,
    val bloodType: String? = null,
    val allergies: String? = null,
    val medicalNotes: String? = null,
    val healthCardNumber: String? = null,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Medicamento incluido en la copia.
 *
 * @property imagePath Ruta relativa dentro del ZIP (`images/...`), o `null`.
 * @property leafletUrl URL externa del prospecto, si existe.
 */
@Serializable
data class BackupMedicationDto(
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
    val deletedAt: String? = null
)

/**
 * Horario de toma incluido en la copia.
 */
@Serializable
data class BackupScheduleDto(
    val id: Int,
    val medicationId: Int,
    val type: String,
    val weekDay: String? = null,
    val intervalDays: Int? = null,
    val dayOfMonth: Int? = null,
    val clampToLastDay: Boolean? = null,
    val monthWeek: Int? = null,
    val onDays: Int? = null,
    val offDays: Int? = null,
    val time: String
)

/**
 * Toma incluida en la copia.
 */
@Serializable
data class BackupIntakeDto(
    val id: Int,
    val medicationId: Int,
    val scheduleId: Int? = null,
    val scheduledAt: String,
    val takenAt: String? = null,
    val medicationNameSnapshot: String,
    val doseSnapshot: Double,
    val unitSnapshot: String,
    val createdAt: String,
    val updatedAt: String
)
