package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.Perfil
import kotlinx.coroutines.flow.Flow

interface PerfilRepository {
    suspend fun insertPerfil(perfil: Perfil)
    suspend fun deletePerfil(perfil: Perfil)
    suspend fun updatePerfil(perfil: Perfil)
    fun getPerfiles(): Flow<List<Perfil>>
}