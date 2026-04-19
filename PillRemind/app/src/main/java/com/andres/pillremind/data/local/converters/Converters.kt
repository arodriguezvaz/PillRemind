package com.andres.pillremind.data.local.converters

import androidx.room.TypeConverter
import com.andres.pillremind.domain.model.TipoUsuario
import java.time.LocalDateTime

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

    // -------- TipoUsuario --------
    @TypeConverter
    fun fromTipoUsuario(tipo: TipoUsuario): String {
        return tipo.name
    }

    @TypeConverter
    fun toTipoUsuario(value: String): TipoUsuario {
        return TipoUsuario.valueOf(value)
    }
}