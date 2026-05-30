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
    supabaseUid = supabaseUid,
    tipoUsuario = tipoUsuario,
    email = email,
    esPremium = esPremium,
    fechaPremium = fechaPremium,
    syncPendiente = syncPendiente,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Convierte un objeto User en un objeto UserEntity compatiblecon la base de datos
 * @return UserEntity
 * @receiver User
 */
fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    supabaseUid = supabaseUid,
    tipoUsuario = tipoUsuario,
    email = email,
    esPremium = esPremium,
    fechaPremium = fechaPremium,
    syncPendiente = syncPendiente,
    createdAt = createdAt,
    updatedAt = updatedAt
)