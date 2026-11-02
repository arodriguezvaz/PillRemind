package com.andres.pillremind.data.mapper

import com.andres.pillremind.data.local.entity.ProfileEntity
import com.andres.pillremind.domain.model.BloodType
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.model.Sex
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas del mapeo entre [ProfileEntity] y [Profile].
 */
class ProfileMapperTest {

    private val birthDate = LocalDate.of(1990, 5, 12)
    private val createdAt = LocalDateTime.of(2026, 10, 1, 9, 0)
    private val updatedAt = LocalDateTime.of(2026, 10, 20, 10, 0)

    @Test
    fun `toDomain conserva los datos medicos del perfil`() {
        val entity = ProfileEntity(
            id = 3,
            usuarioId = 1,
            name = "Ana",
            imageUrl = "/data/foto.jpg",
            birthDate = birthDate,
            sex = Sex.FEMALE,
            bloodType = BloodType.O_POSITIVE,
            allergies = "Penicilina",
            medicalNotes = "Asma",
            healthCardNumber = "SIP-123",
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        val profile = entity.toDomain()

        assertEquals(3, profile.id)
        assertEquals("Ana", profile.name)
        assertEquals(birthDate, profile.birthDate)
        assertEquals(Sex.FEMALE, profile.sex)
        assertEquals(BloodType.O_POSITIVE, profile.bloodType)
        assertEquals("Penicilina", profile.allergies)
        assertEquals("Asma", profile.medicalNotes)
        assertEquals("SIP-123", profile.healthCardNumber)
    }

    @Test
    fun `toEntity conserva los datos medicos del perfil`() {
        val profile = Profile(
            id = 3,
            usuarioId = 1,
            name = "Ana",
            birthDate = birthDate,
            sex = Sex.PREFER_NOT_TO_SAY,
            bloodType = BloodType.AB_NEGATIVE,
            allergies = "Frutos secos",
            medicalNotes = "Hipertensión",
            healthCardNumber = "SIP-456"
        )

        val entity = profile.toEntity()

        assertEquals(birthDate, entity.birthDate)
        assertEquals(Sex.PREFER_NOT_TO_SAY, entity.sex)
        assertEquals(BloodType.AB_NEGATIVE, entity.bloodType)
        assertEquals("Frutos secos", entity.allergies)
        assertEquals("Hipertensión", entity.medicalNotes)
        assertEquals("SIP-456", entity.healthCardNumber)
    }

    @Test
    fun `los campos opcionales vacios se mantienen en null`() {
        val profile = Profile(
            usuarioId = 1,
            name = "Sin datos"
        ).toEntity().toDomain()

        assertNull(profile.birthDate)
        assertNull(profile.sex)
        assertNull(profile.bloodType)
        assertNull(profile.allergies)
        assertNull(profile.medicalNotes)
        assertNull(profile.healthCardNumber)
    }
}
