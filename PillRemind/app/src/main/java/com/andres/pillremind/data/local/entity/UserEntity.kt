package com.andres.pillremind.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.andres.pillremind.domain.model.TipoUsuario
import java.time.LocalDateTime

/**
 * Clase que representa un usuario de la aplicación en la BD
 * @property id Identificador único del usuario
 * @property supabaseUid Identificador único del usuario en Supabase
 * @property tipoUsuario Tipo de usuario, invitado al inciar la app
 * @property email Email del usuario si es registrado, si no null
 * @property esPremium Indica si el usuario es premium
 * @property fechaPremium Fecha de vencimiento del plan premium, si no null
 * @property aceptaComunicaciones Indica si el usuario acepta recibir comunicaciones
 * @property syncPendiente Indica si hay cambios pendientes de sincronización con Supabase
 * @property createdAt Fecha de creación del usuario
 * @property updatedAt Fecha de última actualización del usuario
 */
@Entity(tableName = "usuarios")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val supabaseUid: String? = null,
    val tipoUsuario: TipoUsuario = TipoUsuario.INVITADO,
    val email: String? = null,                 // null si es invitado
    val esPremium: Boolean = false,
    val fechaPremium: LocalDateTime? = null,
    val syncPendiente: Boolean = false,        // para upgrade a premium
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)