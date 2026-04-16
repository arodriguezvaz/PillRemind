package com.andres.pillremind.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "usuario")
data class Usuario(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val email: String,
    val esPremium: Boolean = false,
    val fechaPremium: LocalDateTime? = null,
    val aceptaComunicaciones: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
