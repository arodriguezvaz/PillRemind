/*package com.andres.pillremind.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "toma",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["
            horarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TomaEntity(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val horarioId: UUID,
    val fechaHoraProgramada: LocalDateTime,
    val fechaHoraReal: LocalDateTime? = null,
    val estado: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
*/