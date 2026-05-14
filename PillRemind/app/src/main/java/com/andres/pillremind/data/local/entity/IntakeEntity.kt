package com.andres.pillremind.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Clase que representa una toma de un medicamento de la aplicación en la BD
 * @property id Identificador único de la toma
 * @property scheduleId Identificador del horario asociado
 * @property scheduledAt Fecha y hora programada de la toma
 * @property takenAt Fecha y hora real en la que se realizó la toma (opcional)
 * @property createdAt Fecha de creación del registro
 * @property updatedAt Fecha de última actualización del registro
 */
@Entity(
    tableName = "intakes",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class IntakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val scheduleId: Int,
    val scheduledAt: LocalDateTime,
    val takenAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)