package com.andres.pillremind.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Clase que representa un profile de la aplicación en la BD
 * @property id Identificador único del profile
 * @property name Nombre del profile
 * @property urlImage URL de la imagen del profile
 * @property createdAt Fecha de creación del profile
 * @property updatedAt Fecha de última actualización del profile
 */
@Entity(tableName = "perfiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val urlImage: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
