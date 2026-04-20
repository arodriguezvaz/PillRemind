package com.andres.pillremind.data.repository;

import com.andres.pillremind.data.local.dao.ProfileDao;
import com.andres.pillremind.data.mapper.toDomain
import com.andres.pillremind.data.mapper.toEntity
import com.andres.pillremind.domain.model.Perfil
import com.andres.pillremind.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Clase que implementa la interfaz ProfileRepository
 */
class ProfileRepositoryImpl(private val perfilDao: ProfileDao) : ProfileRepository {
    override suspend fun insertPerfil(perfil: Perfil) {
        return perfilDao.insertPerfil(perfil.toEntity())
    }

    override suspend fun deletePerfil(perfil: Perfil) {
        return perfilDao.deletePerfil(perfil.toEntity())
    }

    override suspend fun updatePerfil(perfil: Perfil) {
        return perfilDao.updatePerfil(perfil.toEntity())
    }

    override fun getPerfil(id: Int): Flow<Perfil> {
        return perfilDao.getPerfil(id).map { it.toDomain() }
    }

    override fun getPerfiles(): Flow<List<Perfil>> {
        return perfilDao.getPerfiles().map { list -> list.map { it.toDomain() } }
    }

}

