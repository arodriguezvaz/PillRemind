package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define el contrato para la gestión de persistencia de los datos de
 */
interface UserRepository {
    /**
     * Inserta un nuevo usuario en la base de datos
     * @param user Usuario a insertar
     */
    suspend fun insertUser(user: User): Long

    /**
     * Actualiza un usuario existente en la base de datos
     * @param user Usuario a actualizar
     */
    suspend fun updateUser(user: User)

    /**
     * Elimina un usuario
     * @param user Usuario a eliminar
     */
    suspend fun deleteUser(user: User)

    /**
     * Comprueba si existe un ususario en la bd y si no lo hace se crea un usuario invitado
     */
    suspend fun crearUserInvitado()

    /**
     * Obtiene el usuario local de Room
     */
    suspend fun getUser(): User?

    /**
     * Observa el usuario desde Room y emite cambios en tiempo real.
     */
    fun getUserFlow(): Flow<User?>

}