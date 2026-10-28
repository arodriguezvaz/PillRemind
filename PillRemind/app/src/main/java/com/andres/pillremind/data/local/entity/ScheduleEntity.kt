package com.andres.pillremind.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.andres.pillremind.domain.model.WeekDay
import java.time.LocalTime

/**
 * Entidad que representa un horario de toma en la base de datos local.
 *
 * @property id Identificador unico del horario.
 * @property medicationId Identificador del medicamento propietario.
 * @property weekDay Dia de la semana en el que se programa la toma.
 * @property time Hora a la que se debe tomar el medicamento.
 */
@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicationId")]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val medicationId: Int,
    val weekDay: WeekDay,
    val time: LocalTime
)