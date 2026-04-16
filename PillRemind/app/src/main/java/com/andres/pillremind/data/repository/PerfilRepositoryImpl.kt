package com.andres.pillremind.data.repository;

import com.andres.pillremind.data.local.dao.PerfilDao;
import com.andres.pillremind.data.mapper.toDomain
import com.andres.pillremind.data.mapper.toEntity
import com.andres.pillremind.domain.model.Perfil
import com.andres.pillremind.domain.repository.PerfilRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PerfilRepositoryImpl(private val perfilDao: PerfilDao) : PerfilRepository {
    override suspend fun insertPerfil(perfil: Perfil) {
        return perfilDao.insertPerfil(perfil.toEntity())
    }

    override suspend fun deletePerfil(perfil: Perfil) {
        return perfilDao.deletePerfil(perfil.toEntity())
    }

    override suspend fun updatePerfil(perfil: Perfil) {
        return perfilDao.updatePerfil(perfil.toEntity())
    }

    override suspend fun getPerfiles(): Flow<List<Perfil>> {
        return perfilDao.getPerfiles().map { list -> list.map { it.toDomain() } }
    }

}

