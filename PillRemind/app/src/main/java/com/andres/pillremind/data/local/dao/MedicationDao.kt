package com.andres.pillremind.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.andres.pillremind.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

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
     * Observa los medicamentos activos de un perfil.
     *
     * @param profileId ID del perfil propietario.
     * @return Flow con la lista de medicamentos no archivados.
     */
    @Query("SELECT * FROM medications WHERE profileId = :profileId AND deletedAt IS NULL")
    fun getMedications(profileId: Int): Flow<List<MedicationEntity>>

    /**
     * Marca un medicamento como archivado sin borrar su historial.
     */
    @Query("UPDATE medications SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :medicationId")
    suspend fun markDeleted(
        medicationId: Int,
        deletedAt: LocalDateTime,
        updatedAt: LocalDateTime
    )

    /**
     * Devuelve un medicamento por su ID en una consulta unica.
     *
     * @param id ID del medicamento.
     * @return Medicamento encontrado o null si no existe.
     */
    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationByIdOnce(id: Int): MedicationEntity?
}
