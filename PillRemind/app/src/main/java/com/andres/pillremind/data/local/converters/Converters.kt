package com.andres.pillremind.data.local.converters

import androidx.room.TypeConverter
import com.andres.pillremind.domain.model.WeekDay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 *Clase que contiene los converters para convertir tipos de datos no soportados por Room
 */
class Converters {

    // -------- LocalDateTime --------
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    //-------LocalDate-----------
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(date: String?): LocalDate? {
        return date?.let { LocalDate.parse(it) }
    }

    //-------LocalTime-------
    @TypeConverter
    fun fromLocalTime(time: LocalTime): String {
        return time.toString()
    }

    @TypeConverter
    fun toLocalTime(time: String): LocalTime {
        return LocalTime.parse(time)
    }

    //------WeekDay-------
    @TypeConverter
    fun fromDiaSemana(dia: WeekDay): String {
        return dia.name
    }

    @TypeConverter
    fun toDiaSemana(value: String): WeekDay {
        return WeekDay.valueOf(value)
    }
}