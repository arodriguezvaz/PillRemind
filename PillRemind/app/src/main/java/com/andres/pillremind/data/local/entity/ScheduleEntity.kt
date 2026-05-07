package com.andres.pillremind.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.andres.pillremind.domain.model.WeekDay
import java.time.LocalTime

/**
 * Clase que representa un horario de la aplicación en la base de datos
 * @property id Identificador único del horario
 * @property medicationId Identificador del medicamento al que pertenece el horario
 * @property weekDay de la semana en el que se programa la toma (recurrencia semanal)
 * @property time Hora a la que se debe tomar el medicamento
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
    ]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val medicationId: Int,
    val weekDay: WeekDay,
    val time: LocalTime
)