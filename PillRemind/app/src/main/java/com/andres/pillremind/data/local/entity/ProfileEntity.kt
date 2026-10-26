package com.andres.pillremind.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entidad que representa un perfil en la base de datos local.
 *
 * @property id Identificador unico del perfil.
 * @property usuarioId Identificador del usuario propietario.
 * @property name Nombre del perfil.
 * @property imageUrl URL de la imagen del perfil (opcional).
 * @property createdAt Fecha de creacion del registro.
 * @property updatedAt Fecha de ultima actualizacion del registro.
 */
@Entity(
    tableName = "profiles",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val name: String,
    val imageUrl: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
