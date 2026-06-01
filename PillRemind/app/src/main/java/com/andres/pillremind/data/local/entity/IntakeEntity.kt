package com.andres.pillremind.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entidad que representa una toma de medicamento en la base de datos local.
 *
 * @property id Identificador unico de la toma.
 * @property scheduleId Identificador del horario asociado.
 * @property supabaseId Identificador remoto en Supabase (opcional).
 * @property scheduledAt Fecha y hora programada de la toma.
 * @property takenAt Fecha y hora real en la que se realizo la toma (opcional).
 * @property createdAt Fecha de creacion del registro.
 * @property updatedAt Fecha de ultima actualizacion del registro.
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
    ],
    indices = [
        Index(
            value = ["scheduleId", "scheduledAt"],
            unique = true
        )
    ]
)
data class IntakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val scheduleId: Int,
    val supabaseId: String? = null,
    val scheduledAt: LocalDateTime,
    val takenAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)