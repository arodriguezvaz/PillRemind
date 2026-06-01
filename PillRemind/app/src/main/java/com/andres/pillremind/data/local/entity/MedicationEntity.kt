package com.andres.pillremind.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entidad que representa un medicamento en la base de datos local.
 *
 * @property id Identificador unico del medicamento.
 * @property profileId Identificador del perfil propietario.
 * @property supabaseId Identificador remoto en Supabase (opcional).
 * @property name Nombre del medicamento.
 * @property dose Dosis del medicamento.
 * @property unit Unidad de medida de la dosis.
 * @property isChronic Indica si el medicamento es cronico.
 * @property startDate Fecha de inicio del tratamiento.
 * @property endDate Fecha de fin del tratamiento (opcional).
 * @property imageUrl URL de la imagen del medicamento (opcional).
 * @property leafletUrl URL del prospecto del medicamento (opcional).
 * @property createdAt Fecha de creacion del registro.
 * @property updatedAt Fecha de ultima actualizacion del registro.
 */
@Entity(
    tableName = "medications",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val profileId: Int,
    val supabaseId: String? = null,
    val name: String,
    val dose: Double,
    val unit: String,
    val isChronic: Boolean = false,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val imageUrl: String? = null,
    val leafletUrl: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)