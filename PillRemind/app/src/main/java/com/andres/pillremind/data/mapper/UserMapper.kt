package com.andres.pillremind.data.mapper

import com.andres.pillremind.data.local.entity.UserEntity
import com.andres.pillremind.domain.model.User

/**
 * Convierte un objeto UserEntity en un objeto User
 * @return User
 * @receiver UserEntity
 */
fun UserEntity.toDomain(): User = User(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Convierte un objeto User en un objeto UserEntity compatible con la base de datos
 * @return UserEntity
 * @receiver User
 */
fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt
)
