package com.andres.pillremind.data.repository

import com.andres.pillremind.domain.repository.AuthRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserSession
import jakarta.inject.Inject

/**
 * Implementación de [AuthRepository] utilizando Supabase Auth.
 *
 * Gestiona el registro, inicio y cierre de sesión de usuarios,
 * así como las operaciones de recuperación de contraseña.
 */
class AuthRepositoryImpl @Inject constructor(
    private val auth: Auth
) : AuthRepository {
    /**
     * Inicia sesión con correo electrónico y contraseña.
     */
    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /**
     * Registra un nuevo usuario mediante correo electrónico y contraseña.
     */
    override suspend fun register(email: String, password: String): Result<Unit> = runCatching {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /**
     * Envía un correo para restablecer la contraseña.
     * NO IMPLEMENTADO
     */
    override suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        auth.resetPasswordForEmail(email)
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    override suspend fun logout(): Result<Unit> = runCatching {
        auth.signOut()
    }

    /**
     * Obtiene la sesión actualmente almacenada por Supabase.
     */
    override fun getCurrentSession(): UserSession? = auth.currentSessionOrNull()
}