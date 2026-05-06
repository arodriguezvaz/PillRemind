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
    suspend fun insertPerfil(profile: Profile)

    /**
     * Actualiza un perfil en la base de datos
     */
    suspend fun deletePerfil(profile: Profile)

    /**
     * Actualiza un perfil en la base de datos
     */
    suspend fun updatePerfil(profile: Profile)

    /**
     * Obtiene un perfil de la base de datos
     */
    fun getPerfiles(): Flow<List<Profile>>

    /**
     * Obtiene un perfil de la base de datos
     */
    fun getPerfilById(id: Int?): Flow<Profile?>
}