package com.andres.pillremind.data.local.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.andres.pillremind.data.local.entity.PerfilEntity
import kotlinx.coroutines.flow.Flow

interface PerfilDao {
    @Insert
    suspend fun insertPerfil(perfil: PerfilEntity)

    @Update
    suspend fun updatePerfil(perfil: PerfilEntity)

    @Delete
    suspend fun deletePerfil(perfil: PerfilEntity)

    @Query("SELECT * FROM perfiles ORDER BY createdAt DESC")
    fun getPerfiles(): Flow<List<PerfilEntity>>
}