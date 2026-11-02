package com.andres.pillremind.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Representa un perfil de usuario en la aplicación.
 *
 * @property id Identificador único del perfil
 * @property name Nombre del perfil
 * @property urlImagen URL de la imagen del perfil
 * @property birthDate Fecha de nacimiento, si se ha informado
 * @property sex Sexo declarado, si se ha informado
 * @property bloodType Grupo sanguíneo, si se ha informado
 * @property allergies Alergias conocidas, si se han informado
 * @property medicalNotes Condiciones o notas médicas relevantes
 * @property healthCardNumber Número de tarjeta sanitaria, si se ha informado
 * @property createdAt Fecha de creación del perfil
 * @property updatedAt Fecha de última actualización del perfil
 */
data class Profile(
    val id: Int = 0,
    val usuarioId: Int,
    val name: String,
    val urlImagen: String? = null,
    val birthDate: LocalDate? = null,
    val sex: Sex? = null,
    val bloodType: BloodType? = null,
    val allergies: String? = null,
    val medicalNotes: String? = null,
    val healthCardNumber: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
