package com.andres.pillremind.data.local.projections

import java.time.LocalDateTime

//TODO añadir nombre del perfil del medicamento par que que se enseñe en la notificación
data class IntakeWithMedication(
    val intakeId: Int,
    val scheduledAt: LocalDateTime,
    val scheduleId: Int,
    val takenAt: LocalDateTime?,
    val medicationName: String,
    val dose: Double,
    val unit: String,
    val imageUrl: String?,
)


fun IntakeWithMedication.visualState(): IntakeVisualState {
    if (takenAt != null) return IntakeVisualState.TAKEN
    return if (LocalDateTime.now() > scheduledAt.plusMinutes(15))
        IntakeVisualState.DELAYED
    else
        IntakeVisualState.PENDING
}


enum class IntakeVisualState { TAKEN, DELAYED, PENDING }