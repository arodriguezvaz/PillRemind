package com.andres.pillremind.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO de horario utilizado para la comunicación con Supabase.
 *
 *  * Se utiliza para serializar y deserializar los datos intercambiados
 *  * entre la aplicación y el backend.
 */
@Serializable
data class ScheduleDto(
    val id: String? = null,
    @SerialName("medication_id")
    val medicationId: String,
    @SerialName("week_day")
    val weekDay: String,
    val time: String  // "HH:mm:ss"
)