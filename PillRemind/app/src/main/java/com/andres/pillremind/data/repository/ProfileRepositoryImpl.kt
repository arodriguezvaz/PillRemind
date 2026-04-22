package com.andres.pillremind.data.repository;

import com.andres.pillremind.data.local.dao.ProfileDao;
import com.andres.pillremind.data.mapper.toDomain
import com.andres.pillremind.data.mapper.toEntity
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Clase que implementa la interfaz ProfileRepository
 */
class ProfileRepositoryImpl(private val perfilDao: ProfileDao) : ProfileRepository {
    override suspend fun insertPerfil(profile: Profile) {
        return perfilDao.insertProfile(profile.toEntity())
    }

    override suspend fun deletePerfil(profile: Profile) {
        return perfilDao.deleteProfile(profile.toEntity())
    }

    override suspend fun updatePerfil(profile: Profile) {
        return perfilDao.updateProfile(profile.toEntity())
    }

    override fun getPerfil(id: Int?): Flow<Profile> {
        return perfilDao.getProfile(id).map { it.toDomain() }
    }

    override fun getPerfiles(): Flow<List<Profile>> {
        return perfilDao.getProfiles().map { list -> list.map { it.toDomain() } }
    }

}

