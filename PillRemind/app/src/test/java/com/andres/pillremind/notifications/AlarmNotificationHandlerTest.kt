package com.andres.pillremind.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.Intake
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Pruebas del procesamiento de alarmas y de la notificación asociada.
 *
 * Se cubren los casos en los que no debe mostrarse el aviso y el caso correcto,
 * además de comprobar que siempre se refresca la ventana de recordatorios.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AlarmNotificationHandlerTest {

    private companion object {
        const val INTAKE_ID = 1
        const val MEDICATION_ID = 10
        const val PROFILE_ID = 20
    }

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val intakeRepository = mockk<IntakeRepository>()
    private val medicationRepository = mockk<MedicationRepository>()
    private val profileRepository = mockk<ProfileRepository>()
    private val reminderScheduler = mockk<ReminderScheduler>()

    private lateinit var handler: AlarmNotificationHandler
    private lateinit var notificationManager: NotificationManager
    private var notificationPermissionGranted = false

    @Before
    fun setUp() {
        handler = AlarmNotificationHandler(
            context = context,
            intakeRepository = intakeRepository,
            medicationRepository = medicationRepository,
            profileRepository = profileRepository,
            reminderScheduler = reminderScheduler
        )
        notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.REMINDERS,
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        coEvery { reminderScheduler.generateWindow(any(), any()) } returns emptyList()
        mockkStatic(ContextCompat::class)
        every { ContextCompat.checkSelfPermission(any(), any()) } answers {
            if (notificationPermissionGranted) {
                PackageManager.PERMISSION_GRANTED
            } else {
                PackageManager.PERMISSION_DENIED
            }
        }
    }

    @After
    fun tearDown() {
        unmockkStatic(ContextCompat::class)
    }

    @Test
    fun `no notifica si la toma no existe`() = runTest {
        coEvery { intakeRepository.getIntakeById(INTAKE_ID) } returns null

        handler.handle(INTAKE_ID)

        assertEquals(0, shadowOf(notificationManager).size())
        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }
    }

    @Test
    fun `no notifica si la toma ya fue registrada`() = runTest {
        coEvery { intakeRepository.getIntakeById(INTAKE_ID) } returns
            createIntake(takenAt = LocalDateTime.now())

        handler.handle(INTAKE_ID)

        assertEquals(0, shadowOf(notificationManager).size())
        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }
    }

    @Test
    fun `no notifica si el medicamento esta archivado`() = runTest {
        coEvery { intakeRepository.getIntakeById(INTAKE_ID) } returns createIntake()
        coEvery { medicationRepository.getMedicationByIdOnce(MEDICATION_ID) } returns
            createMedication(deletedAt = LocalDateTime.now())
        coEvery { profileRepository.getProfileByIdOnce(PROFILE_ID) } returns createProfile()

        handler.handle(INTAKE_ID)

        assertEquals(0, shadowOf(notificationManager).size())
        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }
    }

    @Test
    fun `no notifica si el perfil no existe`() = runTest {
        coEvery { intakeRepository.getIntakeById(INTAKE_ID) } returns createIntake()
        coEvery { medicationRepository.getMedicationByIdOnce(MEDICATION_ID) } returns
            createMedication()
        coEvery { profileRepository.getProfileByIdOnce(PROFILE_ID) } returns null

        handler.handle(INTAKE_ID)

        assertEquals(0, shadowOf(notificationManager).size())
        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }
    }

    @Test
    fun `no notifica si no hay permiso de notificaciones`() = runTest {
        coEvery { intakeRepository.getIntakeById(INTAKE_ID) } returns createIntake()
        coEvery { medicationRepository.getMedicationByIdOnce(MEDICATION_ID) } returns
            createMedication()
        coEvery { profileRepository.getProfileByIdOnce(PROFILE_ID) } returns createProfile()

        handler.handle(INTAKE_ID)

        assertEquals(0, shadowOf(notificationManager).size())
        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }
    }

    @Test
    fun `muestra la notificacion y refresca la ventana cuando la toma es valida`() = runTest {
        notificationPermissionGranted = true
        coEvery { intakeRepository.getIntakeById(INTAKE_ID) } returns createIntake()
        coEvery { medicationRepository.getMedicationByIdOnce(MEDICATION_ID) } returns
            createMedication()
        coEvery { profileRepository.getProfileByIdOnce(PROFILE_ID) } returns createProfile()

        handler.handle(INTAKE_ID)

        assertEquals(1, shadowOf(notificationManager).size())
        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }

        val notification = shadowOf(notificationManager).allNotifications.single()
        assertEquals(
            context.getString(R.string.notification_title, "Ibuprofeno"),
            notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        )
        assertEquals(
            context.getString(R.string.notification_text, "09:00", "600 mg"),
            notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        )
        assertEquals(
            context.getString(R.string.notification_subtext, "Andrés"),
            notification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        )
        assertEquals(
            context.getString(R.string.notification_action_mark_taken),
            notification.actions.single().title.toString()
        )
    }

    @Test
    fun `muestra la dosis en singular cuando la toma es una unidad`() = runTest {
        notificationPermissionGranted = true
        coEvery { intakeRepository.getIntakeById(INTAKE_ID) } returns createIntake()
        coEvery { medicationRepository.getMedicationByIdOnce(MEDICATION_ID) } returns
            createMedication(dose = 1.0, unit = "comprimidos")
        coEvery { profileRepository.getProfileByIdOnce(PROFILE_ID) } returns createProfile()

        handler.handle(INTAKE_ID)

        val notification = shadowOf(notificationManager).allNotifications.single()
        assertEquals(
            context.getString(R.string.notification_text, "09:00", "1 comprimido"),
            notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        )
    }

    @Test
    fun `las unidades singular y plural estan alineadas`() {
        val plural = context.resources.getStringArray(R.array.medication_units)
        val singular = context.resources.getStringArray(R.array.medication_units_singular)

        assertEquals(plural.size, singular.size)
    }

    @Test
    fun `conserva los decimales de la dosis`() = runTest {
        notificationPermissionGranted = true
        coEvery { intakeRepository.getIntakeById(INTAKE_ID) } returns createIntake()
        coEvery { medicationRepository.getMedicationByIdOnce(MEDICATION_ID) } returns
            createMedication(dose = 0.5)
        coEvery { profileRepository.getProfileByIdOnce(PROFILE_ID) } returns createProfile()

        handler.handle(INTAKE_ID)

        val notification = shadowOf(notificationManager).allNotifications.single()
        assertEquals(
            context.getString(R.string.notification_text, "09:00", "0.5 mg"),
            notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        )
    }

    @Test
    fun `refresca la ventana aunque falle la consulta de la toma`() = runTest {
        coEvery { intakeRepository.getIntakeById(INTAKE_ID) } throws IllegalStateException("fallo")

        handler.handle(INTAKE_ID)

        assertEquals(0, shadowOf(notificationManager).size())
        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }
    }

    private fun createIntake(takenAt: LocalDateTime? = null) = Intake(
        id = INTAKE_ID,
        medicationId = MEDICATION_ID,
        scheduledAt = LocalDateTime.of(2026, 10, 29, 9, 0),
        takenAt = takenAt
    )

    private fun createMedication(
        deletedAt: LocalDateTime? = null,
        dose: Double = 600.0,
        unit: String = "mg"
    ) = Medication(
        id = MEDICATION_ID,
        profileId = PROFILE_ID,
        name = "Ibuprofeno",
        dose = dose,
        unit = unit,
        startDate = LocalDate.of(2026, 10, 1),
        deletedAt = deletedAt
    )

    private fun createProfile() = Profile(
        id = PROFILE_ID,
        usuarioId = 1,
        name = "Andrés"
    )
}
