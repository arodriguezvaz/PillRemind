package com.andres.pillremind.domain.model

import java.time.LocalDateTime

data class Profile(
    val id: Int = 0,
    val name: String,
    val urlImagen: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
