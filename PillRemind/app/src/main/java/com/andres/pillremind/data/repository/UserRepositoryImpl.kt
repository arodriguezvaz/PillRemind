package com.andres.pillremind.data.repository

import com.andres.pillremind.data.local.dao.UserDao
import com.andres.pillremind.data.mapper.toDomain
import com.andres.pillremind.data.mapper.toEntity
import com.andres.pillremind.domain.model.User
import com.andres.pillremind.domain.repository.UserRepository
import jakarta.inject.Inject
import java.time.LocalDateTime

/**
 * Implementación de UserRepository que utiliza la base de datos local para
 * acceder a los datos del usuario.
 */
class UserRepositoryImpl @Inject constructor(private val userDao: UserDao) : UserRepository {

    /**
     * Comprueba si existe un usuario en la base de datos y, si no lo hace,
     * crea un usuario invitado.
     */
    override suspend fun crearUserInvitado() {
        val usuarioExistente = userDao.getUser()
        if (usuarioExistente == null) {
            val invitado = User(
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            userDao.insertUser(invitado.toEntity())
        }
    }

    override suspend fun getUser(): User? {
        return userDao.getUser()?.toDomain()
    }
}
