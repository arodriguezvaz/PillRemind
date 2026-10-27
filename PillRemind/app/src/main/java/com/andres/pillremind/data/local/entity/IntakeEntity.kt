package com.andres.pillremind.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entidad que representa una toma de medicamento en la base de datos local.
 *
 * La toma pertenece al medicamento y conserva una copia de los datos de dosis
 * y unidad para que el historial no cambie cuando se edita el medicamento.
 *
 * @property id Identificador unico de la toma.
 * @property medicationId Identificador del medicamento propietario.
 * @property scheduleId Identificador del horario que genero la toma (opcional).
 * @property scheduledAt Fecha y hora programada de la toma.
 * @property takenAt Fecha y hora real en la que se realizo la toma (opcional).
 * @property medicationNameSnapshot Nombre del medicamento al crear la toma.
 * @property doseSnapshot Dosis del medicamento al crear la toma.
 * @property unitSnapshot Unidad de la dosis al crear la toma.
 * @property createdAt Fecha de creacion del registro.
 * @property updatedAt Fecha de ultima actualizacion del registro.
 */
@Entity(
    tableName = "intakes",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(
            value = ["medicationId", "scheduledAt"],
            unique = true
        ),
        Index(value = ["scheduleId"])
    ]
)
data class IntakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val medicationId: Int,
    val scheduleId: Int? = null,
    val scheduledAt: LocalDateTime,
    val takenAt: LocalDateTime? = null,
    val medicationNameSnapshot: String,
    val doseSnapshot: Double,
    val unitSnapshot: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
