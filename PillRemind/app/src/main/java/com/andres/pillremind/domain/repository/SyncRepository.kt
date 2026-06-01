package com.andres.pillremind.domain.repository

import com.andres.pillremind.data.remote.dto.UsuarioDto

interface SyncRepository {
    suspend fun syncUsuario(): Result<Unit>
    suspend fun syncPerfiles(): Result<Unit>
    suspend fun syncMedicamentos(): Result<Unit>
    suspend fun syncSchedules(): Result<Unit>
    suspend fun syncIntakes(): Result<Unit>
    suspend fun syncCompleto(): Result<Unit>
    suspend fun borrarLocalYDescargar(): Result<Unit>
    suspend fun downloadCompleto(): Result<Unit>
    suspend fun getUsuarioRemoto(supabaseUid: String): UsuarioDto?
    suspend fun deleteProfile(supabaseId: String): Result<Unit>
    suspend fun deleteMedication(supabaseId: String): Result<Unit>
}

