package com.andres.pillremind.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.andres.pillremind.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define los métodos para interactuar con la base de datos.
 */
@Dao
interface ProfileDao {
    /**
     * Inserta un perfil y devuelve el ID generado.
     *
     * @param perfil Entidad del perfil a insertar.
     * @return ID generado por la base de datos.
     */
    @Insert
    suspend fun insertProfile(perfil: ProfileEntity): Long

    /**
     * Actualiza un perfil existente.
     *
     * @param perfil Entidad del perfil con los cambios.
     */
    @Update
    suspend fun updateProfile(perfil: ProfileEntity)

    /**
     * Elimina un perfil existente.
     *
     * @param perfil Entidad del perfil a eliminar.
     */
    @Delete
    suspend fun deleteProfile(perfil: ProfileEntity)

    /**
     * Observa todos los perfiles, ordenados por fecha de creacion descendente.
     *
     * @return Flow con la lista de perfiles.
     */
    @Query("SELECT * FROM profiles ORDER BY createdAt DESC")
    fun getProfiles(): Flow<List<ProfileEntity>>

    /**
     * Observa un perfil por su ID.
     *
     * @param id ID del perfil.
     * @return Flow con el perfil o null si no existe.
     */
    @Query("SELECT * FROM profiles WHERE id = :id")
    fun getProfileById(id: Int?): Flow<ProfileEntity?>

    /**
     * Devuelve un perfil por su ID en una consulta unica.
     *
     * @param id ID del perfil.
     * @return Perfil encontrado o null si no existe.
     */
    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileByIdOnce(id: Int?): ProfileEntity?
}