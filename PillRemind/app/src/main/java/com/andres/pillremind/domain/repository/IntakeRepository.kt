package com.andres.pillremind.domain.repository

import com.andres.pillremind.data.local.projections.IntakeWithMedication
import com.andres.pillremind.domain.model.Intake
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Interfaz que define las operaciones de acceso a datos relacionadas con tomas
 */
interface IntakeRepository {
    /**
     * Inserta una toma de medicación en la base de datos
     */
    suspend fun insertIntake(intake: Intake): Long

    /**
     * Elimina una toma de medicación de la base de datos
     */
    suspend fun deleteIntake(intake: Intake)

    /**
     * Actualiza una toma de medicación en la base de datos
     */
    suspend fun updateIntake(intake: Intake)

    /**
     * Obtiene una toma de medicación de la base de datos
     */
    suspend fun getIntakeById(intakeId: Int): Intake?

    /**
     * Obtiene todas las tomas de un perfil para hoy
     */
    fun getIntakesForToday(
        today: LocalDate,
        profileId: Int
    ): Flow<List<IntakeWithMedication>>

    /**
     * Crea las tomas de hoy
     */
    suspend fun createIntakes(desde: LocalDate, hasta: LocalDate): List<Intake>
    suspend fun getLastGeneratedDate(): LocalDate

    suspend fun saveLastGeneratedDate(fecha: LocalDate)

    suspend fun getAllByUser(userId: Int): List<Intake>

}

