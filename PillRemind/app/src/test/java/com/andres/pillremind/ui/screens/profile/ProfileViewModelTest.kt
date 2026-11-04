package com.andres.pillremind.ui.screens.profile

import android.content.Context
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.BloodType
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.model.Sex
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.notifications.ReminderNotifications
import com.andres.pillremind.notifications.ReminderScheduler
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Pruebas de la gestión de los datos médicos del perfil.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val profileRepository = mockk<ProfileRepository>(relaxed = true)
    private val medicationRepository = mockk<MedicationRepository>(relaxed = true)
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val reminderScheduler = mockk<ReminderScheduler>(relaxed = true)
    private val notifications = mockk<ReminderNotifications>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    private lateinit var viewModel: ProfileViewModel

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val profileId = 10
    private val userId = 1
    private val createdAt = LocalDateTime.of(2026, 10, 1, 9, 0)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { sessionManager.activeUserId } returns flowOf(userId)
        every { sessionManager.activeProfileId } returns flowOf(profileId)
        viewModel = ProfileViewModel(
            profileRepository = profileRepository,
            medicationRepository = medicationRepository,
            sessionManager = sessionManager,
            reminderScheduler = reminderScheduler,
            notifications = notifications,
            context = context
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addProfile guarda los datos medicos y normaliza los textos vacios`() = runTest {
        val inserted = slot<Profile>()
        coEvery { profileRepository.insertProfile(capture(inserted)) } returns 1L

        viewModel.addProfile(
            nombre = "  Luis  ",
            birthDate = LocalDate.of(1985, 3, 20),
            sex = Sex.MALE,
            bloodType = BloodType.A_POSITIVE,
            allergies = "   ",
            medicalNotes = "  Diabetes  ",
            healthCardNumber = "  SIP-123  "
        )

        assertEquals("Luis", inserted.captured.name)
        assertEquals(LocalDate.of(1985, 3, 20), inserted.captured.birthDate)
        assertEquals(Sex.MALE, inserted.captured.sex)
        assertEquals(BloodType.A_POSITIVE, inserted.captured.bloodType)
        assertNull(inserted.captured.allergies)
        assertEquals("Diabetes", inserted.captured.medicalNotes)
        assertEquals("SIP-123", inserted.captured.healthCardNumber)
    }

    @Test
    fun `updateProfile conserva createdAt y normaliza los textos vacios`() = runTest {
        val currentProfile = Profile(
            id = profileId,
            usuarioId = userId,
            name = "Ana",
            createdAt = createdAt,
            updatedAt = createdAt
        )
        coEvery { profileRepository.getProfileByIdOnce(profileId) } returns currentProfile
        coEvery { profileRepository.updateProfile(any()) } just Runs
        val updated = slot<Profile>()

        viewModel.updateProfile(
            nombre = "  Ana María  ",
            birthDate = LocalDate.of(1990, 5, 12),
            sex = Sex.FEMALE,
            bloodType = BloodType.O_NEGATIVE,
            allergies = "  ",
            medicalNotes = "  Asma  ",
            healthCardNumber = "  "
        )

        coVerify { profileRepository.updateProfile(capture(updated)) }
        assertEquals("Ana María", updated.captured.name)
        assertEquals(createdAt, updated.captured.createdAt)
        assertEquals(LocalDate.of(1990, 5, 12), updated.captured.birthDate)
        assertEquals(Sex.FEMALE, updated.captured.sex)
        assertEquals(BloodType.O_NEGATIVE, updated.captured.bloodType)
        assertNull(updated.captured.allergies)
        assertEquals("Asma", updated.captured.medicalNotes)
        assertNull(updated.captured.healthCardNumber)
    }

    @Test
    fun `deleteProfile borra las imagenes solo despues del commit`() = runTest {
        val filesDir = temporaryFolder.newFolder("files")
        every { context.filesDir } returns filesDir
        val profileImage = File(filesDir, "profile.jpg").apply { writeText("perfil") }
        val medicationImage = File(filesDir, "medication.jpg").apply { writeText("medi") }
        val profile = Profile(
            id = profileId,
            usuarioId = userId,
            name = "Ana",
            urlImagen = profileImage.absolutePath,
            createdAt = createdAt,
            updatedAt = createdAt,
        )
        coEvery { medicationRepository.getMedicationsOnce(profileId) } returns listOf(
            Medication(
                id = 1,
                profileId = profileId,
                name = "Ibuprofeno",
                dose = 600.0,
                unit = "mg",
                startDate = LocalDate.of(2026, 1, 1),
                imageUrl = medicationImage.absolutePath,
            )
        )
        coEvery { profileRepository.deleteProfile(profile) } just Runs
        var deleted = false

        viewModel.deleteProfile(profile) { deleted = true }

        assertTrue(deleted)
        assertFalse(profileImage.exists())
        assertFalse(medicationImage.exists())
        coVerify(exactly = 1) { profileRepository.deleteProfile(profile) }
        verify(exactly = 1) { notifications.cancelAll() }
    }
}
