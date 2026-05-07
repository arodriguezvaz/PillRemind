package com.andres.pillremind.data.repository

import com.andres.pillremind.data.local.dao.ProfileDao;
import com.andres.pillremind.data.mapper.toDomain
import com.andres.pillremind.data.mapper.toEntity
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementación del repositorio de perfiles.
 * Gestiona el acceso a datos relacionados con perfiles.
 */
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileRepository {

    override suspend fun insertProfile(profile: Profile) {
        profileDao.insertProfile(profile.toEntity())
    }

    override suspend fun deleteProfile(profile: Profile) {
        profileDao.deleteProfile(profile.toEntity())
    }

    override suspend fun updateProfile(profile: Profile) {
        profileDao.updateProfile(profile.toEntity())
    }

    override fun getProfileById(id: Int): Flow<Profile?> {
        return profileDao.getProfileById(id).map { it?.toDomain() }
    }

    override fun getProfiles(): Flow<List<Profile>> {
        return profileDao.getProfiles()
            .map { list -> list.map { it.toDomain() } }
    }
}