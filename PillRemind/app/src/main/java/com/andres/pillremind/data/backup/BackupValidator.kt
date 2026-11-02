package com.andres.pillremind.data.backup

import java.time.LocalDateTime

/**
 * Valida el contenido de una copia antes de modificar la base de datos.
 *
 * Se comprueban el formato, la versión, los identificadores, las referencias
 * entre tablas, las fechas y las rutas de imagen. Cualquier problema lanza una
 * [BackupException] con un código que la UI traduce a un texto propio.
 */
object BackupValidator {

    private val imagePathRegex =
        Regex("^images/[A-Za-z0-9._-]+\\.(jpg|jpeg|png|webp)$", RegexOption.IGNORE_CASE)

    /**
     * Valida una copia completa.
     *
     * @param file Contenido deserializado del archivo.
     */
    fun validate(file: BackupFileDto) {
        if (file.format != BackupFormat.NAME) {
            throw BackupException(
                BackupError.INVALID_FILE,
                "El archivo no es una copia de seguridad de PillRemind"
            )
        }
        if (file.formatVersion != BackupFormat.VERSION) {
            throw BackupException(
                BackupError.UNSUPPORTED_VERSION,
                "Versión de copia no compatible"
            )
        }
        if (file.users.isEmpty()) {
            throw BackupException(
                BackupError.INVALID_DATA,
                "La copia no contiene ningún usuario"
            )
        }

        requireUnique(file.users.map { it.id }, "usuarios")
        requireUnique(file.profiles.map { it.id }, "perfiles")
        requireUnique(file.medications.map { it.id }, "medicamentos")
        requireUnique(file.schedules.map { it.id }, "horarios")
        requireUnique(file.intakes.map { it.id }, "tomas")

        val userIds = file.users.map { it.id }.toSet()
        val profileIds = file.profiles.map { it.id }.toSet()
        val medicationIds = file.medications.map { it.id }.toSet()
        val scheduleIds = file.schedules.map { it.id }.toSet()

        file.users.forEach { user ->
            requirePositiveId(user.id, "usuario")
            parseBackupDateTime(user.createdAt, "usuario")
            parseBackupDateTime(user.updatedAt, "usuario")
        }

        file.profiles.forEach { profile ->
            requirePositiveId(profile.id, "perfil")
            if (profile.usuarioId !in userIds) {
                throw invalidData("El perfil ${profile.name} referencia un usuario inexistente")
            }
            if (profile.name.isBlank()) {
                throw invalidData("La copia contiene un perfil sin nombre")
            }
            validateImagePath(profile.imagePath, "el perfil ${profile.name}")
            parseBackupDateTime(profile.createdAt, "perfil")
            parseBackupDateTime(profile.updatedAt, "perfil")
        }

        file.medications.forEach { medication ->
            requirePositiveId(medication.id, "medicamento")
            if (medication.profileId !in profileIds) {
                throw invalidData(
                    "El medicamento ${medication.name} referencia un perfil inexistente"
                )
            }
            if (medication.name.isBlank()) {
                throw invalidData("La copia contiene un medicamento sin nombre")
            }
            if (!medication.dose.isFinite()) {
                throw invalidData("La dosis de ${medication.name} no es válida")
            }
            validateImagePath(medication.imagePath, "el medicamento ${medication.name}")
            parseBackupDate(medication.startDate, "medicamento")
            medication.endDate?.let { parseBackupDate(it, "medicamento") }
            parseBackupDateTime(medication.createdAt, "medicamento")
            parseBackupDateTime(medication.updatedAt, "medicamento")
            medication.deletedAt?.let { parseBackupDateTime(it, "medicamento") }
        }

        file.schedules.forEach { schedule ->
            requirePositiveId(schedule.id, "horario")
            if (schedule.medicationId !in medicationIds) {
                throw invalidData("Un horario referencia un medicamento inexistente")
            }
            schedule.toRule()
            parseBackupTime(schedule.time, "horario")
        }

        val intakeKeys = mutableSetOf<Pair<Int, LocalDateTime>>()
        file.intakes.forEach { intake ->
            requirePositiveId(intake.id, "toma")
            if (intake.medicationId !in medicationIds) {
                throw invalidData("Una toma referencia un medicamento inexistente")
            }
            intake.scheduleId?.let { scheduleId ->
                if (scheduleId !in scheduleIds) {
                    throw invalidData("Una toma referencia un horario inexistente")
                }
            }
            if (!intake.doseSnapshot.isFinite()) {
                throw invalidData("Una toma contiene una dosis no válida")
            }
            val scheduledAt = parseBackupDateTime(intake.scheduledAt, "toma")
            intake.takenAt?.let { parseBackupDateTime(it, "toma") }
            parseBackupDateTime(intake.createdAt, "toma")
            parseBackupDateTime(intake.updatedAt, "toma")

            val key = intake.medicationId to scheduledAt
            if (!intakeKeys.add(key)) {
                throw invalidData("La copia contiene tomas duplicadas")
            }
        }
    }

    private fun requireUnique(ids: List<Int>, table: String) {
        if (ids.size != ids.toSet().size) {
            throw invalidData("La copia contiene identificadores duplicados en $table")
        }
    }

    private fun requirePositiveId(id: Int, table: String) {
        if (id <= 0) {
            throw invalidData("Identificador inválido en $table")
        }
    }

    private fun validateImagePath(path: String?, owner: String) {
        if (path == null) return
        if (!imagePathRegex.matches(path) || path.contains("..")) {
            throw invalidData("Ruta de imagen inválida en $owner")
        }
    }

    private fun invalidData(message: String): BackupException =
        BackupException(BackupError.INVALID_DATA, message)
}
