package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.Profile
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define los métodos para interactuar con la base de datos de perfiles
 */
interface ProfileRepository {
    suspend fun insertPerfil(profile: Profile)
    suspend fun deletePerfil(profile: Profile)
    suspend fun updatePerfil(profile: Profile)
    fun getPerfiles(): Flow<List<Profile>>
    fun getPerfil(id: Int?): Flow<Profile>
}