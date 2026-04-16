/*package com.andres.pillremind.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "medicamento",
    foreignKeys = [
        ForeignKey(
            entity = PerfilEntity::class,
            parentColumns = ["id"],
            childColumns = ["perfilId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MedicamentoEntity(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val perfilId: UUID,
    val nombre: String,
    val dosis: Double,
    val unidad: String,
    val esCronico: Boolean = false,
    val fechaInicio: LocalDate,
    val fechaFin: LocalDate? = null,
    val urlImagen: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
*/