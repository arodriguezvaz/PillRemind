package com.andres.pillremind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.andres.pillremind.data.local.entity.UserEntity

/**
 * DAO para acceder a los datos del usuario local en la base de datos.
 */
@Dao
interface UserDao {
    /**
     * Inserta un usuario y devuelve el ID generado.
     *
     * @param user Entidad del usuario a insertar.
     * @return ID generado por la base de datos.
     */
    @Insert
    suspend fun insertUser(user: UserEntity): Long

    /**
     * Devuelve el usuario local, si existe.
     *
     * @return Usuario encontrado o null si no hay registros.
     */
    @Query("SELECT * FROM usuarios LIMIT 1")
    suspend fun getUser(): UserEntity?
}
