package com.andres.pillremind.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IntakeDto(
    val id: String? = null,
    @SerialName("schedule_id")
    val scheduleId: String,
    @SerialName("scheduled_at")
    val scheduledAt: String,
    @SerialName("taken_at")
    val takenAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)