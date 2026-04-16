package com.andres.pillremind.domain.model

import java.util.UUID

data class Horario(
    val id: UUID = UUID.randomUUID(),
    val medicamentoId: UUID,
    val diaSemana: Int,
    val hora: String
)
