package com.andres.pillremind.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.andres.pillremind.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define los métodos para interactuar con la base de datos
 */
@Dao
interface MedicationDao {
    /**
     * Inserta un medication en la base de datos
     * @param medication Medication a insertar
     */
    @Insert
    suspend fun insertMedication(medication: MedicationEntity): Long

    /**
     * Actualiza un medication en la base de datos
     * @param medication Medication a actualizar
     */
    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    /**
     * Elimina un medication de la base de datos
     * @param medication Medication a eliminar
     */
    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)

    /**
     * Obtiene un medication por su ID
     */
    @Query("SELECT * FROM medications WHERE id = :id")
    fun getMedicationById(id: Int?): Flow<MedicationEntity?>

    /**
     * Obtiene todos los medication de la base de datos
     */
    @Query("SELECT * FROM medications WHERE profileId = :profileId")
    fun getMedications(profileId: Int): Flow<List<MedicationEntity>>
}
