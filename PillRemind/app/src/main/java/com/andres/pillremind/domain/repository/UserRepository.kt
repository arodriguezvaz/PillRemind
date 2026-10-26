package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.User

/**
 * Interfaz que define el contrato para la gestión del usuario local.
 */
interface UserRepository {
    /**
     * Comprueba si existe un usuario en la base de datos y, si no lo hace,
     * crea un usuario invitado.
     */
    suspend fun crearUserInvitado()

    /**
     * Obtiene el usuario local de Room.
     */
    suspend fun getUser(): User?
}
