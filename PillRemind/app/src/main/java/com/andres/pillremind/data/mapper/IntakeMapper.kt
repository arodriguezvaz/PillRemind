package com.andres.pillremind.data.mapper

import com.andres.pillremind.data.local.entity.IntakeEntity
import com.andres.pillremind.domain.model.Intake

/**
 * Convierte un objeto IntakeEntity en un objeto Intake
 * @return Intake
 * @receiver IntakeEntity
 */
fun IntakeEntity.toDomain(): Intake = Intake(
    id = id,
    scheduleId = scheduleId,
    supabaseId = supabaseId,
    scheduledAt = scheduledAt,
    takenAt = takenAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Convierte un objeto Intake en un objeto IntakeEntity compatible con la base de datos
 * @return IntakeEntity
 * @receiver Intake
 */
fun Intake.toEntity(): IntakeEntity = IntakeEntity(
    id = id,
    scheduleId = scheduleId,
    supabaseId = supabaseId,
    scheduledAt = scheduledAt,
    takenAt = takenAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)