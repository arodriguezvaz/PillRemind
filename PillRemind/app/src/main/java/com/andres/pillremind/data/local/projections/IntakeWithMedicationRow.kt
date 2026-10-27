package com.andres.pillremind.data.local.projections

import java.time.LocalDateTime

/**
 * Proyección de Room utilizada para leer una toma junto con los datos del
 * medicamento y del perfil.
 *
 * Esta clase pertenece a la capa de datos; el repositorio la mapea a
 * [com.andres.pillremind.domain.model.IntakeWithMedication].
 */
data class IntakeWithMedicationRow(
    val intakeId: Int,
    val medicationId: Int,
    val scheduledAt: LocalDateTime,
    val scheduleId: Int?,
    val takenAt: LocalDateTime?,
    val medicationName: String,
    val dose: Double,
    val unit: String,
    val imageUrl: String?,
    val profileName: String
)
