package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.Perfil
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define los métodos para interactuar con la base de datos de perfiles
 */
interface ProfileRepository {
    suspend fun insertPerfil(perfil: Perfil)
    suspend fun deletePerfil(perfil: Perfil)
    suspend fun updatePerfil(perfil: Perfil)
    fun getPerfiles(): Flow<List<Perfil>>
    fun getPerfil(id: Int): Flow<Perfil>
}