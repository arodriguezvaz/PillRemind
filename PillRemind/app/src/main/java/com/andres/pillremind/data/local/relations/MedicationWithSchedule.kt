package com.andres.pillremind.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.andres.pillremind.data.local.entity.MedicationEntity
import com.andres.pillremind.data.local.entity.ScheduleEntity

/**
 * Relación entre un medicamento y sus horarios programados.
 * Permite obtener todos los schedules asociados a un medicamento.
 */
data class MedicationWithSchedule(
    @Embedded val medication: MedicationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "medicationId"
    )
    val schedules: List<ScheduleEntity>
)