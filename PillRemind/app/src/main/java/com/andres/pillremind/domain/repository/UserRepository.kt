package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define los métodos para acceder a los datos de los usuarios en la base de datos
 */
interface UserRepository {
    suspend fun insertUser(user: User): Long
    suspend fun updateUser(user: User)
    suspend fun deleteUser(user: User)
    suspend fun crearUserInvitado()
    suspend fun getUser(): User?
    
    fun getUserFlow(): Flow<User?>

}