package com.andres.pillremind.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.andres.pillremind.domain.model.BloodType
import com.andres.pillremind.domain.model.Sex
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entidad que representa un perfil en la base de datos local.
 *
 * @property id Identificador unico del perfil.
 * @property usuarioId Identificador del usuario propietario.
 * @property name Nombre del perfil.
 * @property imageUrl URL de la imagen del perfil (opcional).
 * @property birthDate Fecha de nacimiento (opcional).
 * @property sex Sexo declarado (opcional).
 * @property bloodType Grupo sanguíneo (opcional).
 * @property allergies Alergias conocidas (opcional).
 * @property medicalNotes Condiciones o notas médicas (opcional).
 * @property healthCardNumber Número de tarjeta sanitaria (opcional).
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
    ],
    indices = [Index("usuarioId")]
)
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val name: String,
    val imageUrl: String? = null,
    val birthDate: LocalDate? = null,
    val sex: Sex? = null,
    val bloodType: BloodType? = null,
    val allergies: String? = null,
    val medicalNotes: String? = null,
    val healthCardNumber: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
