package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.Profile
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define los métodos para interactuar con la base de datos de perfiles
 */
interface ProfileRepository {
    /**
     * Inserta un perfil en la base de datos
     */
    suspend fun insertProfile(profile: Profile): Long

    /**
     * Actualiza un perfil en la base de datos
     */
    suspend fun deleteProfile(profile: Profile)

    /**
     * Actualiza un perfil en la base de datos
     */
    suspend fun updateProfile(profile: Profile)

    /**
     * Obtiene un perfil de la base de datos
     */
    fun getProfiles(): Flow<List<Profile>>

    /**
     * Obtiene un perfil de la base de datos
     */
    fun getProfileById(id: Int): Flow<Profile?>
    suspend fun getProfileByIdOnce(id: Int): Profile?
    suspend fun getAllByUser(userId: Int): List<Profile>
    suspend fun deleteAllByUsuario(usuarioId: Int)
}