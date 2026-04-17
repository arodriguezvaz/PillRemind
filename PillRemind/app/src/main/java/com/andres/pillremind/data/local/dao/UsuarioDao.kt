package com.andres.pillremind.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.andres.pillremind.data.local.entity.UsuarioEntity

/**
 * Interfaz que define los métodos para acceder a los datos de los usuarios en la base de datos
 * @property insertUsuario Inserta un usuario en la base de datos
 * @property updateUsuario Actualiza un usuario en la base de datos
 * @property deleteUsuario Elimina un usuario de la base de datos
 * @property getUsuario Obtiene un usuario de la base de datos, que en local solo puede haber uno
 */
@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertUsuario(usuario: UsuarioEntity)

    @Update
    suspend fun updateUsuario(usuario: UsuarioEntity)

    @Delete
    suspend fun deleteUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios LIMIT 1")
    fun getUsuario(): UsuarioEntity?
}

