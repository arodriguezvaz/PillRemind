package com.andres.pillremind.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.andres.pillremind.data.local.entity.IntakeEntity
import com.andres.pillremind.data.local.entity.ScheduleEntity

/**
 * Relación entre un horario y su toma.
 * Permite obtener todas las tomas asociados a un horario.
 */
data class ScheduleWithIntakes(
    @Embedded val schedule: ScheduleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "scheduleId"
    )
    val intakes: List<IntakeEntity>
)