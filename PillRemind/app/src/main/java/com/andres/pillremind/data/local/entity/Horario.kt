package com.andres.pillremind.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID
@Entity(
    tableName = "horario",
    foreignKeys = [
        ForeignKey(
            entity = Medicamento::class,
            parentColumns = ["id"],
            childColumns = ["medicamentoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Horario(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val medicamentoId: UUID,
    val diaSemana: Int,
    val hora: String
)
