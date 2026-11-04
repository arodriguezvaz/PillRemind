package com.andres.pillremind.data.mapper

import com.andres.pillremind.data.local.projections.AdherenceRow
import com.andres.pillremind.domain.model.MedicationAdherence

/**
 * Convierte la fila agregada de Room en el modelo de dominio.
 */
fun AdherenceRow.toDomain(): MedicationAdherence = MedicationAdherence(
    medicationId = medicationId,
    medicationName = medicationName,
    dose = dose,
    unit = unit,
    imageUrl = imageUrl,
    isArchived = deletedAt != null,
    total = total,
    taken = taken,
    missed = missed
)
