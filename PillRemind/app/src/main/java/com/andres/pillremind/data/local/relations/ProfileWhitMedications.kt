package com.andres.pillremind.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.andres.pillremind.data.local.entity.MedicationEntity
import com.andres.pillremind.data.local.entity.ProfileEntity

data class ProfileWhitMedications(
    @Embedded val profile: ProfileEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "profileId"
    )
    val medications: List<MedicationEntity>
)