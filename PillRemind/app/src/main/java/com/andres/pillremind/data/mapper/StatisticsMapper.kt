package com.andres.pillremind.data.mapper

import com.andres.pillremind.data.local.projections.StatisticsIntakeRow
import com.andres.pillremind.domain.model.StatisticsIntake

/**
 * Convierte la fila de estadísticas de Room en el modelo de dominio.
 *
 * @return Toma preparada para el cálculo de estadísticas.
 */
fun StatisticsIntakeRow.toDomain(): StatisticsIntake = StatisticsIntake(
    intakeId = intakeId,
    medicationId = medicationId,
    scheduledAt = scheduledAt,
    takenAt = takenAt,
    medicationName = medicationName,
    dose = dose,
    unit = unit,
    imageUrl = imageUrl,
    isArchived = deletedAt != null
)
