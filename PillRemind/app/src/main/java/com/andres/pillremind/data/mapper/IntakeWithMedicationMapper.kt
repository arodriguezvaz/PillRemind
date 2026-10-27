package com.andres.pillremind.data.mapper

import com.andres.pillremind.data.local.projections.IntakeWithMedicationRow
import com.andres.pillremind.domain.model.IntakeWithMedication

/**
 * Convierte la proyección de Room en el modelo de dominio.
 */
fun IntakeWithMedicationRow.toDomain(): IntakeWithMedication = IntakeWithMedication(
    intakeId = intakeId,
    medicationId = medicationId,
    scheduledAt = scheduledAt,
    scheduleId = scheduleId,
    takenAt = takenAt,
    medicationName = medicationName,
    dose = dose,
    unit = unit,
    imageUrl = imageUrl,
    profileName = profileName
)
