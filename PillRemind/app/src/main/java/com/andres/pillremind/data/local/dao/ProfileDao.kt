package com.andres.pillremind.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.andres.pillremind.data.local.entity.PerfilEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define los métodos para interacatuar con la base de datos
 */
@Dao
interface ProfileDao {
    /**
     * Inserta un perfil en la base de datos
     * @param perfil Perfil a insertar
     */
    @Insert
    suspend fun insertPerfil(perfil: PerfilEntity)

    /**
     * Actualiza un perfil en la base de datos
     * @param perfil Perfil a actualizar
     */
    @Update
    suspend fun updatePerfil(perfil: PerfilEntity)

    /**
     * Elimina un perfil de la base de datos
     * @param perfil Perfil a eliminar
     */
    @Delete
    suspend fun deletePerfil(perfil: PerfilEntity)

    /**
     * Obtiene todos los perfiles usando Flow, ordenados por fecha de creación descendente
     */
    @Query("SELECT * FROM perfiles ORDER BY createdAt DESC")
    fun getPerfiles(): Flow<List<PerfilEntity>>

    /**
     * Obtiene un perfil por su ID
     */
    @Query("SELECT * FROM perfiles WHERE id = :id")
    fun getPerfil(id: Int): PerfilEntity
}