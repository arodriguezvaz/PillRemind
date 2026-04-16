package com.andres.pillremind.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID
@Entity(
    tableName = "perfil",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Perfil(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val usuarioId: UUID,
    val nombre: String,
    val urlImagen: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
