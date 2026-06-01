package com.andres.pillremind.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MedicationDto(
    val id: String? = null,
    @SerialName("profile_id")
    val profileId: String,
    val name: String,
    val dose: Double,
    val unit: String,
    @SerialName("is_chronic")
    val isChronic: Boolean = false,
    @SerialName("start_date")
    val startDate: String,
    @SerialName("end_date")
    val endDate: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("leaflet_url")
    val leafletUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)