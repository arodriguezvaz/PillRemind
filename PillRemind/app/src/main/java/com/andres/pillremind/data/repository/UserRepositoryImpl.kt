package com.andres.pillremind.data.repository

import com.andres.pillremind.data.local.dao.UserDao
import com.andres.pillremind.data.mapper.toDomain
import com.andres.pillremind.data.mapper.toEntity
import com.andres.pillremind.domain.model.TipoUsuario
import com.andres.pillremind.domain.model.User
import com.andres.pillremind.domain.repository.UserRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

/**
 * Implementación de la interfaz UserRepository que utiliza la base de datos local para acceder a los datos de los usuarios
 */
class UserRepositoryImpl @Inject constructor(private val userDao: UserDao) : UserRepository {
    override suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user.toEntity())
    }

    override suspend fun updateUser(user: User) {
        return userDao.updateUser(user.toEntity())
    }

    override suspend fun deleteUser(user: User) {
        return userDao.deleteUser(user.toEntity())
    }

    override suspend fun crearUserInvitado() {
        val usuarioExistente = userDao.getUser()
        if (usuarioExistente == null) {
            val invitado = User(
                tipoUsuario = TipoUsuario.INVITADO,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            userDao.insertUser(invitado.toEntity())
        }
    }

    override suspend fun getUser(): User? {
        return userDao.getUser()?.toDomain()
    }

    override fun getUserFlow(): Flow<User?> = userDao.getUserFlow().map { it?.toDomain() }

}
