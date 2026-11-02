package com.andres.pillremind.domain.model

/**
 * Sexo declarado en el perfil.
 *
 * Es un dato opcional y solo se usa como información del perfil; la app no
 * calcula dosis ni tratamientos a partir de él.
 */
enum class Sex {
    /** Sexo femenino. */
    FEMALE,

    /** Sexo masculino. */
    MALE,

    /** Otro sexo o identidad. */
    OTHER,

    /** El usuario prefiere no indicarlo. */
    PREFER_NOT_TO_SAY
}
