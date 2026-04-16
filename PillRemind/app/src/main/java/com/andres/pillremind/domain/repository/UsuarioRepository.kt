package com.andres.pillremind.domain.repository

import com.andres.pillremind.data.local.dao.UsuarioDao
import com.andres.pillremind.domain.model.Usuario

/**
 * Interfaz que define los métodos para acceder a los datos de los usuarios en la base de datos
 */
interface UsuarioRepository {
    suspend fun insertUsuario(usuario: Usuario)
    suspend fun updateUsuario(usuario: Usuario)
    suspend fun deleteUsuario(usuario: Usuario)
    suspend fun crearUsuarioInvitado()

}