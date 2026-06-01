package com.andres.pillremind.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.andres.pillremind.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para acceder a los datos de usuario en la base de datos local.
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
     * Actualiza un usuario existente.
     *
     * @param user Entidad del usuario con los cambios.
     */
    @Update
    suspend fun updateUser(user: UserEntity)

    /**
     * Elimina un usuario existente.
     *
     * @param user Entidad del usuario a eliminar.
     */
    @Delete
    suspend fun deleteUser(user: UserEntity)

    /**
     * Devuelve el usuario local, si existe.
     *
     * @return Usuario encontrado o null si no hay registros.
     */
    @Query("SELECT * FROM usuarios LIMIT 1")
    suspend fun getUser(): UserEntity?

    /**
     * Observa el usuario local, si existe.
     *
     * @return Flow con el usuario o null si no hay registros.
     */
    @Query("SELECT * FROM usuarios LIMIT 1")
    fun getUserFlow(): Flow<UserEntity?>

    /**
     * Elimina todos los perfiles de un usuario.
     *
     * @param usuarioId ID del usuario propietario.
     */
    @Query("DELETE FROM profiles WHERE usuarioId = :usuarioId")
    suspend fun deleteAllByUsuario(usuarioId: Int)
}
