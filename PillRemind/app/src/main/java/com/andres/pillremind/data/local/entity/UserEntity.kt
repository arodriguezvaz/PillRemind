package com.andres.pillremind.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Clase que representa al usuario local propietario de los perfiles en la BD.
 *
 * @property id Identificador único del usuario
 * @property createdAt Fecha de creación del usuario
 * @property updatedAt Fecha de última actualización del usuario
 */
@Entity(tableName = "usuarios")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
