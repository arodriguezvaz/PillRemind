package com.andres.pillremind.domain.model

/**
 * Grupo sanguíneo declarado en el perfil.
 *
 * Es un dato opcional pensado como información útil en una emergencia.
 */
enum class BloodType {
    /** A positivo. */
    A_POSITIVE,

    /** A negativo. */
    A_NEGATIVE,

    /** B positivo. */
    B_POSITIVE,

    /** B negativo. */
    B_NEGATIVE,

    /** AB positivo. */
    AB_POSITIVE,

    /** AB negativo. */
    AB_NEGATIVE,

    /** O positivo. */
    O_POSITIVE,

    /** O negativo. */
    O_NEGATIVE
}
