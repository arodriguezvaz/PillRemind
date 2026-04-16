package com.andres.pillremind.data.mapper

import com.andres.pillremind.data.local.entity.PerfilEntity
import com.andres.pillremind.domain.model.Perfil

/**
 * Convierte un objeto PerfilEnity en un objeto Entity
 * @return Perfil
 * @receiver PerfilEntity
 */
fun PerfilEntity.toDomain(): Perfil = Perfil(
    id = id,
    nombre = nombre,
    urlImagen = urlImagen,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Convierte un objeto Perfil en un objeto PerfilEntity compatible con la base de datos
 * @return PerfilEntity
 * @receiver Perfil
 */
fun Perfil.toEntity(): PerfilEntity = PerfilEntity(
    id = id,
    nombre = nombre,
    urlImagen = urlImagen,
    createdAt = createdAt,
    updatedAt = updatedAt
)