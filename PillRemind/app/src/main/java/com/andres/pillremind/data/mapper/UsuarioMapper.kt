package com.andres.pillremind.data.mapper

import com.andres.pillremind.data.local.entity.UsuarioEntity
import com.andres.pillremind.domain.model.Usuario

/**
 * Convierte un objeto UsuarioEntity en un objeto Usuario
 * @return Usuario
 * @receiver UsuarioEntity
 */
fun UsuarioEntity.toDomain(): Usuario = Usuario(
    id = id,
    //supabaseUid=supabaseUid,
    tipoUsuario = tipoUsuario,
    email = email,
    esPremium = esPremium,
    fechaPremium = fechaPremium,
    aceptaComunicaciones = aceptaComunicaciones,
    syncPendiente = syncPendiente,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Convierte un objeto Usuario en un objeto UsuarioEntity compatiblecon la base de datos
 * @return UsuarioEntity
 * @receiver Usuario
 */
fun Usuario.toEntity(): UsuarioEntity = UsuarioEntity(
    id = id,
    //supabaseUid=supabaseUid,
    tipoUsuario = tipoUsuario,
    email = email,
    esPremium = esPremium,
    fechaPremium = fechaPremium,
    aceptaComunicaciones = aceptaComunicaciones,
    syncPendiente = syncPendiente,
    createdAt = createdAt,
    updatedAt = updatedAt
)