package com.andres.pillremind.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.andres.pillremind.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para gestionar medicamentos en la base de datos local.
 */
@Dao
interface MedicationDao {
    /**
     * Inserta un medicamento y devuelve el ID generado.
     *
     * @param medication Entidad del medicamento a insertar.
     * @return ID generado por la base de datos.
     */
    @Insert
    suspend fun insertMedication(medication: MedicationEntity): Long

    /**
     * Actualiza un medicamento existente.
     *
     * @param medication Entidad del medicamento con los cambios.
     */
    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    /**
     * Elimina un medicamento existente.
     *
     * @param medication Entidad del medicamento a eliminar.
     */
    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)

    /**
     * Observa un medicamento por su ID.
     *
     * @param id ID del medicamento.
     * @return Flow con el medicamento o null si no existe.
     */
    @Query("SELECT * FROM medications WHERE id = :id")
    fun getMedicationById(id: Int?): Flow<MedicationEntity?>

    /**
     * Observa los medicamentos de un perfil.
     *
     * @param profileId ID del perfil propietario.
     * @return Flow con la lista de medicamentos del perfil.
     */
    @Query("SELECT * FROM medications WHERE profileId = :profileId")
    fun getMedications(profileId: Int): Flow<List<MedicationEntity>>

    /**
     * Devuelve todos los medicamentos pertenecientes a un usuario.
     *
     * @param usuarioId ID del usuario propietario.
     * @return Lista con los medicamentos del usuario.
     */
    @Query("SELECT * FROM medications WHERE profileId IN (SELECT id FROM profiles WHERE usuarioId = :usuarioId)")
    suspend fun getAllByUser(usuarioId: Int): List<MedicationEntity>

    /**
     * Devuelve un medicamento por su ID en una consulta unica.
     *
     * @param id ID del medicamento.
     * @return Medicamento encontrado o null si no existe.
     */
    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationByIdOnce(id: Int): MedicationEntity?

    /**
     * Elimina todos los medicamentos de un usuario.
     *
     * @param usuarioId ID del usuario propietario.
     */
    @Query("DELETE FROM medications WHERE profileId IN (SELECT id FROM profiles WHERE usuarioId = :usuarioId)")
    suspend fun deleteAllByUsuario(usuarioId: Int)
}
