package com.andres.pillremind.domain.model

import java.time.LocalTime


data class Schedule(
    val id: Int = 0,
    val medicationId: Int,
    val weekDay: WeekDay,
    val time: LocalTime
)
