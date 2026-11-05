package com.andres.pillremind.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Clase que representa un medicamento en la aplicación
 * @property id Identificador único del medicamento
 * @property profileId Identificador del perfil al que pertenece el medicamento
 * @property name Nombre del medicamento
 * @property dose Dosis del medicamento
 * @property unit Unidad de medida de la dosis
 * @property isChronic Si el medicamento es crónico
 * @property startDate Fecha de inicio del medicamento
 * @property endDate Fecha de fin del medicamento
 * @property imageUrl URL de la imagen del medicamento
 * @property leafletUrl URL del prospecto del medicamento
 * @property createdAt Fecha de creación del medicamento
 * @property updatedAt Fecha de última actualización del medicamento
 * @property deletedAt Fecha en la que se archivó el medicamento; null si está activo
 */
data class Medication(
    val id: Int = 0,
    val profileId: Int,
    val name: String,
    val dose: Double,
    val unit: String,
    val isChronic: Boolean = false,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val imageUrl: String? = null,
    val leafletUrl: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val deletedAt: LocalDateTime? = null
)