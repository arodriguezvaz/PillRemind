package com.andres.pillremind.data.repository

import com.andres.pillremind.data.local.dao.UsuarioDao
import com.andres.pillremind.data.mapper.toEntity
import com.andres.pillremind.domain.model.TipoUsuario
import com.andres.pillremind.domain.model.Usuario
import com.andres.pillremind.domain.repository.UsuarioRepository
import java.time.LocalDateTime

/**
 * Implementación de la interfaz UsuarioRepository que utiliza la base de datos local para acceder a los datos de los usuarios
 */
class UsuarioRepositoryImpl(private val usuarioDao: UsuarioDao) : UsuarioRepository {
    override suspend fun insertUsuario(usuario: Usuario) {
        return usuarioDao.insertUsuario(usuario.toEntity())
    }

    override suspend fun updateUsuario(usuario: Usuario) {
        return usuarioDao.updateUsuario(usuario.toEntity())
    }

    override suspend fun deleteUsuario(usuario: Usuario) {
        return usuarioDao.deleteUsuario(usuario.toEntity())
    }

    override suspend fun crearUsuarioInvitado() {
        val usuarioExistente = usuarioDao.getUsuario()
        if (usuarioExistente == null) {
            val invitado = Usuario(
                tipoUsuario = TipoUsuario.INVITADO,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            usuarioDao.insertUsuario(invitado.toEntity())
        }
    }
}
