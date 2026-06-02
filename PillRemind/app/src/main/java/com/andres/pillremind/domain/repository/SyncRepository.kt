package com.andres.pillremind.domain.repository

import com.andres.pillremind.data.remote.dto.UsuarioDto

interface SyncRepository {
    /***
     * Sincroniza los datos del usuario con la base de datos remota
     */
    suspend fun syncUsuario(): Result<Unit>

    /**
     * Sincroniza los datos de los perfiles con la base de datos remota
     */
    suspend fun syncPerfiles(): Result<Unit>

    /**
     * Sincroniza los datos de las tomas con la base de datos remota
     */
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

