package com.andres.pillremind.notifications

import com.andres.pillremind.domain.model.WeekDay
import com.andres.pillremind.domain.model.toDayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

fun nextTriggerMillis(weekDay: WeekDay, time: LocalTime): Long {
    val now = LocalDateTime.now()
    val targetDow = weekDay.toDayOfWeek()

    var next = now
        .with(TemporalAdjusters.nextOrSame(targetDow))
        .withHour(time.hour)
        .withMinute(time.minute)
        .withSecond(0)
        .withNano(0)

    if (!next.isAfter(now)) {
        next = next.plusWeeks(1)
    }

    return next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}