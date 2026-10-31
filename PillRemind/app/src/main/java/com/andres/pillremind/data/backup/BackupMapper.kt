package com.andres.pillremind.data.backup

import com.andres.pillremind.data.local.entity.IntakeEntity
import com.andres.pillremind.data.local.entity.MedicationEntity
import com.andres.pillremind.data.local.entity.ProfileEntity
import com.andres.pillremind.data.local.entity.ScheduleEntity
import com.andres.pillremind.data.local.entity.UserEntity
import com.andres.pillremind.domain.model.WeekDay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

/**
 * Conjunto de entidades listas para sustituir el contenido de la base de datos.
 */
data class BackupEntities(
    val users: List<UserEntity>,
    val profiles: List<ProfileEntity>,
    val medications: List<MedicationEntity>,
    val schedules: List<ScheduleEntity>,
    val intakes: List<IntakeEntity>
)

/**
 * Datos leídos de la base de datos para construir una copia de seguridad.
 */
data class BackupSnapshot(
    val users: List<UserEntity>,
    val profiles: List<ProfileEntity>,
    val medications: List<MedicationEntity>,
    val schedules: List<ScheduleEntity>,
    val intakes: List<IntakeEntity>
)

/**
 * Convierte el snapshot de la base de datos en el DTO que se serializa.
 *
 * @param imagePaths Mapa de ruta absoluta de imagen a ruta relativa dentro del ZIP.
 * @param appVersion Versión de la aplicación que genera la copia.
 * @param exportedAt Momento de la exportación.
 */
fun BackupSnapshot.toBackupFile(
    imagePaths: Map<String, String>,
    appVersion: String,
    exportedAt: LocalDateTime = LocalDateTime.now()
): BackupFileDto = BackupFileDto(
    format = BackupFormat.NAME,
    formatVersion = BackupFormat.VERSION,
    appVersion = appVersion,
    exportedAt = exportedAt.toString(),
    users = users.map { it.toBackupDto() },
    profiles = profiles.map { it.toBackupDto(imagePaths) },
    medications = medications.map { it.toBackupDto(imagePaths) },
    schedules = schedules.map { it.toBackupDto() },
    intakes = intakes.map { it.toBackupDto() }
)

/**
 * Convierte el DTO importado en entidades de Room.
 *
 * @param imagePaths Mapa de ruta relativa del ZIP a ruta absoluta restaurada.
 */
fun BackupFileDto.toEntities(imagePaths: Map<String, String>): BackupEntities = BackupEntities(
    users = users.map { it.toEntity() },
    profiles = profiles.map { it.toEntity(imagePaths) },
    medications = medications.map { it.toEntity(imagePaths) },
    schedules = schedules.map { it.toEntity() },
    intakes = intakes.map { it.toEntity() }
)

private fun UserEntity.toBackupDto(): BackupUserDto = BackupUserDto(
    id = id,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

private fun ProfileEntity.toBackupDto(imagePaths: Map<String, String>): BackupProfileDto =
    BackupProfileDto(
        id = id,
        usuarioId = usuarioId,
        name = name,
        imagePath = imageUrl?.let { imagePaths[it] },
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )

private fun MedicationEntity.toBackupDto(imagePaths: Map<String, String>): BackupMedicationDto =
    BackupMedicationDto(
        id = id,
        profileId = profileId,
        name = name,
        dose = dose,
        unit = unit,
        isChronic = isChronic,
        startDate = startDate.toString(),
        endDate = endDate?.toString(),
        imagePath = imageUrl?.let { imagePaths[it] },
        leafletUrl = leafletUrl,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        deletedAt = deletedAt?.toString()
    )

private fun ScheduleEntity.toBackupDto(): BackupScheduleDto = BackupScheduleDto(
    id = id,
    medicationId = medicationId,
    weekDay = weekDay.name,
    time = time.toString()
)

private fun IntakeEntity.toBackupDto(): BackupIntakeDto = BackupIntakeDto(
    id = id,
    medicationId = medicationId,
    scheduleId = scheduleId,
    scheduledAt = scheduledAt.toString(),
    takenAt = takenAt?.toString(),
    medicationNameSnapshot = medicationNameSnapshot,
    doseSnapshot = doseSnapshot,
    unitSnapshot = unitSnapshot,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

private fun BackupUserDto.toEntity(): UserEntity = UserEntity(
    id = id,
    createdAt = parseBackupDateTime(createdAt, "usuario"),
    updatedAt = parseBackupDateTime(updatedAt, "usuario")
)

private fun BackupProfileDto.toEntity(imagePaths: Map<String, String>): ProfileEntity =
    ProfileEntity(
        id = id,
        usuarioId = usuarioId,
        name = name,
        imageUrl = resolveImagePath(imagePath, imagePaths),
        createdAt = parseBackupDateTime(createdAt, "perfil"),
        updatedAt = parseBackupDateTime(updatedAt, "perfil")
    )

private fun BackupMedicationDto.toEntity(imagePaths: Map<String, String>): MedicationEntity =
    MedicationEntity(
        id = id,
        profileId = profileId,
        name = name,
        dose = dose,
        unit = unit,
        isChronic = isChronic,
        startDate = parseBackupDate(startDate, "medicamento"),
        endDate = endDate?.let { parseBackupDate(it, "medicamento") },
        imageUrl = resolveImagePath(imagePath, imagePaths),
        leafletUrl = leafletUrl,
        createdAt = parseBackupDateTime(createdAt, "medicamento"),
        updatedAt = parseBackupDateTime(updatedAt, "medicamento"),
        deletedAt = deletedAt?.let { parseBackupDateTime(it, "medicamento") }
    )

private fun BackupScheduleDto.toEntity(): ScheduleEntity = ScheduleEntity(
    id = id,
    medicationId = medicationId,
    weekDay = parseBackupWeekDay(weekDay, "horario"),
    time = parseBackupTime(time, "horario")
)

private fun BackupIntakeDto.toEntity(): IntakeEntity = IntakeEntity(
    id = id,
    medicationId = medicationId,
    scheduleId = scheduleId,
    scheduledAt = parseBackupDateTime(scheduledAt, "toma"),
    takenAt = takenAt?.let { parseBackupDateTime(it, "toma") },
    medicationNameSnapshot = medicationNameSnapshot,
    doseSnapshot = doseSnapshot,
    unitSnapshot = unitSnapshot,
    createdAt = parseBackupDateTime(createdAt, "toma"),
    updatedAt = parseBackupDateTime(updatedAt, "toma")
)

private fun resolveImagePath(
    archivePath: String?,
    imagePaths: Map<String, String>
): String? {
    if (archivePath == null) return null
    return imagePaths[archivePath]
        ?: throw BackupException(
            BackupError.MISSING_IMAGES,
            "La copia no contiene la imagen $archivePath"
        )
}

/**
 * Parsea una fecha y hora ISO del archivo de copia.
 *
 * @param value Texto a parsear.
 * @param field Nombre del campo, usado en el mensaje de error.
 */
fun parseBackupDateTime(value: String, field: String): LocalDateTime =
    try {
        LocalDateTime.parse(value)
    } catch (e: DateTimeParseException) {
        throw BackupException(
            BackupError.INVALID_DATA,
            "Fecha y hora inválida en $field",
            e
        )
    }

/**
 * Parsea una fecha ISO del archivo de copia.
 */
fun parseBackupDate(value: String, field: String): LocalDate =
    try {
        LocalDate.parse(value)
    } catch (e: DateTimeParseException) {
        throw BackupException(
            BackupError.INVALID_DATA,
            "Fecha inválida en $field",
            e
        )
    }

/**
 * Parsea una hora ISO del archivo de copia.
 */
fun parseBackupTime(value: String, field: String): LocalTime =
    try {
        LocalTime.parse(value)
    } catch (e: DateTimeParseException) {
        throw BackupException(
            BackupError.INVALID_DATA,
            "Hora inválida en $field",
            e
        )
    }

/**
 * Parsea el nombre de un día de la semana.
 */
fun parseBackupWeekDay(value: String, field: String): WeekDay =
    try {
        WeekDay.valueOf(value)
    } catch (e: IllegalArgumentException) {
        throw BackupException(
            BackupError.INVALID_DATA,
            "Día de la semana inválido en $field",
            e
        )
    }
