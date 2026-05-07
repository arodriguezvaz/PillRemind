package com.andres.pillremind.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.andres.pillremind.data.local.entity.MedicationEntity
import com.andres.pillremind.data.local.entity.ProfileEntity

/**
 * Relación entre un perfil y sus medicamentos.
 * Permite obtener todos los medicamentos asociados a un perfil.
 */
data class ProfileWithMedications(
    @Embedded val profile: ProfileEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "profileId"
    )
    val medications: List<MedicationEntity>
)