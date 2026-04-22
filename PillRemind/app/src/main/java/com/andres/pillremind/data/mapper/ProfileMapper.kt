package com.andres.pillremind.data.mapper

import com.andres.pillremind.data.local.entity.ProfileEntity
import com.andres.pillremind.domain.model.Profile

/**
 * Convierte un objeto PerfilEnity en un objeto Entity
 * @return Profile
 * @receiver ProfileEntity
 */
fun ProfileEntity.toDomain(): Profile = Profile(
    id = id,
    name = name,
    urlImagen = urlImage,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Convierte un objeto Profile en un objeto ProfileEntity compatible con la base de datos
 * @return ProfileEntity
 * @receiver Profile
 */
fun Profile.toEntity(): ProfileEntity = ProfileEntity(
    id = id,
    name = name,
    urlImage = urlImagen,
    createdAt = createdAt,
    updatedAt = updatedAt
)