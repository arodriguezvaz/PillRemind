package com.andres.pillremind.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.andres.pillremind.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define los métodos para acceder a los datos de los usuarios en la base de datos
 * @property insertUser Inserta un usuario en la base de datos
 * @property updateUser Actualiza un usuario en la base de datos
 * @property deleteUser Elimina un usuario de la base de datos
 * @property getUser Obtiene un usuario de la base de datos, que en local solo puede haber uno
 */
@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM usuarios LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Query("SELECT * FROM usuarios LIMIT 1")
    fun getUserFlow(): Flow<UserEntity?>
}

