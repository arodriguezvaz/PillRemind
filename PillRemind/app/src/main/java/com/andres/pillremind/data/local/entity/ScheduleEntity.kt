package com.andres.pillremind.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.andres.pillremind.domain.model.ScheduleType
import com.andres.pillremind.domain.model.WeekDay
import java.time.LocalTime

/**
 * Entidad que representa un horario de toma en la base de datos local.
 *
 * Los campos específicos de cada regla son opcionales y solo se usan según
 * [type]. La validación de combinaciones se hace en la capa de dominio.
 *
 * @property id Identificador unico del horario.
 * @property medicationId Identificador del medicamento propietario.
 * @property type Tipo de recurrencia.
 * @property weekDay Dia de la semana para reglas semanales o mensuales por dia.
 * @property intervalDays Intervalo en dias para [ScheduleType.EVERY_N_DAYS].
 * @property dayOfMonth Dia del mes para [ScheduleType.MONTHLY_DAY].
 * @property clampToLastDay Si se usa el ultimo dia en meses mas cortos.
 * @property monthWeek Ocurrencia mensual: 1..5, o -1 para el ultimo.
 * @property onDays Dias de toma en un ciclo.
 * @property offDays Dias de descanso en un ciclo.
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
    val type: ScheduleType = ScheduleType.WEEKLY,
    val weekDay: WeekDay? = null,
    val intervalDays: Int? = null,
    val dayOfMonth: Int? = null,
    val clampToLastDay: Boolean? = null,
    val monthWeek: Int? = null,
    val onDays: Int? = null,
    val offDays: Int? = null,
    val time: LocalTime
)
