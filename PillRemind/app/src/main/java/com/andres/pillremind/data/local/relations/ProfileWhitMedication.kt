package com.andres.pillremind.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.andres.pillremind.data.local.entity.MedicationEntity
import com.andres.pillremind.domain.model.Profile

data class ProfileWhitMedications(
    @Embedded val profile: Profile,
    @Relation(
        parentColumn = "profileId",
        entityColumn = "userCreatorId"
    )
    val medications: List<MedicationEntity>
)
