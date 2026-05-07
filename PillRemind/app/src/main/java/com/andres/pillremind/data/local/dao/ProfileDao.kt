package com.andres.pillremind.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.andres.pillremind.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define los métodos para interacatuar con la base de datos
 */
@Dao
interface ProfileDao {
    /**
     * Inserta un profile en la base de datos
     * @param perfil Profile a insertar
     */
    @Insert
    suspend fun insertProfile(perfil: ProfileEntity)

    /**
     * Actualiza un profile en la base de datos
     * @param perfil Profile a actualizar
     */
    @Update
    suspend fun updateProfile(perfil: ProfileEntity)

    /**
     * Elimina un profile de la base de datos
     * @param perfil Profile a eliminar
     */
    @Delete
    suspend fun deleteProfile(perfil: ProfileEntity)

    /**
     * Obtiene todos los perfiles usando Flow, ordenados por fecha de creación descendente
     */
    @Query("SELECT * FROM profiles ORDER BY createdAt DESC")
    fun getProfiles(): Flow<List<ProfileEntity>>

    /**
     * Obtiene un profile por su ID
     */
    @Query("SELECT * FROM profiles WHERE id = :id")
    fun getProfileById(id: Int?): Flow<ProfileEntity?>
}