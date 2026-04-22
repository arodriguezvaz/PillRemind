package com.andres.pillremind.data.mapper

import com.andres.pillremind.data.local.entity.MedicationEntity
import com.andres.pillremind.domain.model.Medication
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Convierte un objeto MedicationEntity en un objeto Entity
 * @return Medication
 * @receiver MedicationEntity
 */
fun MedicationEntity.toDomain(): Medication = Medication(
    id = id,
    profileId = profileId,
    name = name,
    dose = dose,
    unit = unit,
    isChronic = isChronic,
    startDate = startDate,
    endDate = endDate,
    imageUrl = imageUrl,
    leafletUrl = leafletUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Convierte un objeto Medication en un objeto MedicationEntity compatible con la base de datos
 * @return MedicationEntity
 * @receiver Medication
 */
fun Medication.toEntity(): MedicationEntity = MedicationEntity(
    id = id,
    profileId = profileId,
    name = name,
    dose = dose,
    unit = unit,
    isChronic = isChronic,
    startDate = startDate,
    endDate = endDate,
    imageUrl = imageUrl,
    leafletUrl = leafletUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)