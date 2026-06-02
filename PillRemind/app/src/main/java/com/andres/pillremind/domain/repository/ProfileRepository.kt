package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.Profile
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define los métodos para interactuar con la base de datos de perfiles
 */
interface ProfileRepository {
    /**
     * Inserta un perfil en la base de datos
     * @param profile Perfil a insertar
     */
    suspend fun insertProfile(profile: Profile): Long

    /**
     * Actualiza un perfil en la base de datos
     * @param profile Perfil a actualizar
     */
    suspend fun deleteProfile(profile: Profile)

    /**
     * Actualiza un perfil en la base de datos
     * @param profile Perfil a actualizar
     */
    suspend fun updateProfile(profile: Profile)

    /**
     * Obtiene un perfil de la base de datos
     */
    fun getProfiles(): Flow<List<Profile>>

    /**
     * Obtiene un perfil de la base de datos Y reacciona a cambios
     * @param id ID del perfil
     */
    fun getProfileById(id: Int): Flow<Profile?>

    /**
     * Obtiene un perfil de la base de datos
     * @param id ID del perfil
     */
    suspend fun getProfileByIdOnce(id: Int): Profile?

    /**
     * Obtiene todos los perfiles de un usuario
     * @param userId ID del usuario propietario
     */
    suspend fun getAllByUser(userId: Int): List<Profile>

    /**
     * Elimina todos los perfiles de un usuario
     * @param usuarioId ID del usuario propietario
     */
    suspend fun deleteAllByUsuario(usuarioId: Int)
}