package com.andres.pillremind.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.andres.pillremind.domain.model.WeekDay
import java.time.LocalTime

@Entity(tableName = "schedule")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val medicationId: Int,
    val weekDay: WeekDay,
    val time: LocalTime
)