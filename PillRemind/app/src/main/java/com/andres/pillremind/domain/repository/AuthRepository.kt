package com.andres.pillremind.domain.repository

import io.github.jan.supabase.auth.user.UserSession

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    fun getCurrentSession(): UserSession?
}