package com.andres.pillremind.domain.repository

import io.github.jan.supabase.auth.user.UserSession

interface AuthRepository {
    /**
     * Inicia sesión con correo electrónico y contraseña.
     */
    suspend fun login(email: String, password: String): Result<Unit>

    /**
     * Registra un nuevo usuario mediante correo electrónico y contraseña.
     * @param email Correo electrónico del usuario.
     * @param password Contraseña del usuario.
     */
    suspend fun register(email: String, password: String): Result<Unit>

    /**
     * Envía un correo para restablecer la contraseña.
     * @param email Correo electrónico del usuario.
     * NO IMPLEMENTADO
     */
    suspend fun resetPassword(email: String): Result<Unit>

    /**
     * Cierra la sesión del usuario actual.
     */
    suspend fun logout(): Result<Unit>

    /**
     * Obtiene la sesión actualmente almacenada por Supabase.
     */
    fun getCurrentSession(): UserSession?
}