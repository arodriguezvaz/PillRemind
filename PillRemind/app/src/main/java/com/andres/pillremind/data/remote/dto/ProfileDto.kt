package com.andres.pillremind.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO de perfil utilizado para la comunicación con Supabase.
 *
 *  * Se utiliza para serializar y deserializar los datos intercambiados
 *  * entre la aplicación y el backend.
 */
@Serializable
data class ProfileDto(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    val name: String,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
