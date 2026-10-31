package com.andres.pillremind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.andres.pillremind.data.local.entity.IntakeEntity
import com.andres.pillremind.data.local.entity.MedicationEntity
import com.andres.pillremind.data.local.entity.ProfileEntity
import com.andres.pillremind.data.local.entity.ScheduleEntity
import com.andres.pillremind.data.local.entity.UserEntity
import java.time.LocalDateTime

/**
 * DAO específico para la exportación e importación de copias de seguridad.
 *
 * Centraliza las lecturas completas y las operaciones de reemplazo de todas las
 * tablas. La importación se ejecuta siempre dentro de una transacción.
 */
@Dao
interface BackupDao {

    @Query("SELECT * FROM usuarios")
    suspend fun getUsersOnce(): List<UserEntity>

    @Query("SELECT * FROM profiles")
    suspend fun getProfilesOnce(): List<ProfileEntity>

    @Query("SELECT * FROM medications")
    suspend fun getMedicationsOnce(): List<MedicationEntity>

    @Query("SELECT * FROM schedules")
    suspend fun getSchedulesOnce(): List<ScheduleEntity>

    @Query("SELECT * FROM intakes")
    suspend fun getIntakesOnce(): List<IntakeEntity>

    /**
     * Devuelve los IDs de las tomas que todavía tienen alarma pendiente.
     */
    @Query("SELECT id FROM intakes WHERE scheduledAt > :from")
    suspend fun getFutureIntakeIds(from: LocalDateTime): List<Int>

    @Query("DELETE FROM intakes")
    suspend fun deleteIntakes()

    @Query("DELETE FROM schedules")
    suspend fun deleteSchedules()

    @Query("DELETE FROM medications")
    suspend fun deleteMedications()

    @Query("DELETE FROM profiles")
    suspend fun deleteProfiles()

    @Query("DELETE FROM usuarios")
    suspend fun deleteUsers()

    @Insert
    suspend fun insertUsers(users: List<UserEntity>)

    @Insert
    suspend fun insertProfiles(profiles: List<ProfileEntity>)

    @Insert
    suspend fun insertMedications(medications: List<MedicationEntity>)

    @Insert
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Insert
    suspend fun insertIntakes(intakes: List<IntakeEntity>)
}
