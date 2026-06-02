package com.andres.pillremind.data.remote.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO de usuario utilizado para la comunicación con Supabase.
 *
 *  * Se utiliza para serializar y deserializar los datos intercambiados
 *  * entre la aplicación y el backend.
 */
@Serializable
data class UsuarioDto(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("tipo_usuario")
    val tipoUsuario: String = "REGISTRADO",
    val email: String? = null,
    @SerialName("es_premium")
    @EncodeDefault
    val esPremium: Boolean = false,
    @SerialName("acepta_comunicaciones")
    val aceptaComunicaciones: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)