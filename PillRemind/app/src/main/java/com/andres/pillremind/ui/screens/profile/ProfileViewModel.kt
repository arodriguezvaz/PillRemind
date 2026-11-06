package com.andres.pillremind.ui.screens.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.utils.copyImageToInternalStorage
import com.andres.pillremind.domain.model.BloodType
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.model.Sex
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ViewModel para la gestión de perfiles.
 *
 * Permite crear, actualizar, eliminar y observar el perfil activo usando
 * [ProfileRepository] y [SessionManager].
 *
 * @property profileRepository Repositorio de perfiles.
 * @property sessionManager Gestor de sesión con el perfil activo.
 * @property reminderScheduler Coordinador de alarmas de recordatorio.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val sessionManager: SessionManager,
    private val reminderScheduler: ReminderScheduler,
    @ApplicationContext private val context: Context
) : ViewModel() {

    /**
     * Flujo con el ID del usuario activo.
     */
    val activeUserId: Flow<Int?> = sessionManager.activeUserId

    /**
     * Agrega un nuevo perfil usando el repositorio.
     *
     * @param nombre Nombre del perfil.
     * @param photoUri URI opcional de la imagen del perfil.
     * @param birthDate Fecha de nacimiento opcional.
     * @param sex Sexo declarado opcional.
     * @param bloodType Grupo sanguíneo opcional.
     * @param allergies Alergias conocidas opcionales.
     * @param medicalNotes Notas médicas opcionales.
     * @param healthCardNumber Número de tarjeta sanitaria opcional.
     * @param onSaved Callback ejecutado cuando el perfil ya está guardado.
     */
    fun addProfile(
        nombre: String,
        photoUri: Uri? = null,
        birthDate: LocalDate? = null,
        sex: Sex? = null,
        bloodType: BloodType? = null,
        allergies: String? = null,
        medicalNotes: String? = null,
        healthCardNumber: String? = null,
        onSaved: () -> Unit = {}
    ) {
        val trimmedNombre = nombre.trim()
        if (trimmedNombre.isNotEmpty()) {
            viewModelScope.launch {
                val userId = activeUserId.firstOrNull() ?: return@launch
                val imagePath = photoUri?.let {
                    copyImageToInternalStorage(context, it)
                }
                profileRepository.insertProfile(
                    Profile(
                        usuarioId = userId,
                        name = trimmedNombre,
                        urlImagen = imagePath,
                        birthDate = birthDate,
                        sex = sex,
                        bloodType = bloodType,
                        allergies = allergies.normalizedOrNull(),
                        medicalNotes = medicalNotes.normalizedOrNull(),
                        healthCardNumber = healthCardNumber.normalizedOrNull()
                    )
                )
                onSaved()
            }
        }
    }

    /**
     * Elimina un perfil y limpia la sesión activa si corresponde.
     *
     * @param profile Perfil que se desea eliminar.
     * @param onDeleted Callback ejecutado cuando el borrado se completa.
     */
    fun deleteProfile(profile: Profile, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            // Cancelar las alarmas antes de que Room borre las tomas en cascada.
            runCatching { reminderScheduler.cancelFutureForProfile(profile.id) }
                .onFailure {
                    Log.e(
                        "PillDebug",
                        "No se pudieron cancelar las alarmas del perfil ${profile.id}",
                        it
                    )
                }

            profile.urlImagen?.let { File(it).delete() }
            profileRepository.deleteProfile(profile)

            // Solo se limpia la sesión si el perfil borrado era el activo.
            if (sessionManager.activeProfileId.firstOrNull() == profile.id) {
                sessionManager.clearActiveProfile()
            }

            onDeleted()
        }
    }

    val profileId = sessionManager.activeProfileId

    /**
     * Actualiza un perfil existente.
     *
     * @param nombre Nombre actualizado del perfil.
     * @param newPhotoUri URI nueva del picker.
     * @param currentImagePath Ruta actual guardada en Room.
     * @param birthDate Fecha de nacimiento opcional.
     * @param sex Sexo declarado opcional.
     * @param bloodType Grupo sanguíneo opcional.
     * @param allergies Alergias conocidas opcionales.
     * @param medicalNotes Notas médicas opcionales.
     * @param healthCardNumber Número de tarjeta sanitaria opcional.
     * @param onSaved Callback ejecutado cuando el perfil ya está actualizado;
     *   recibe la ruta de la imagen resultante.
     */
    fun updateProfile(
        nombre: String,
        newPhotoUri: Uri? = null,
        currentImagePath: String? = null,
        birthDate: LocalDate? = null,
        sex: Sex? = null,
        bloodType: BloodType? = null,
        allergies: String? = null,
        medicalNotes: String? = null,
        healthCardNumber: String? = null,
        onSaved: (String?) -> Unit = {}
    ) {
        val trimmedNombre = nombre.trim()
        if (trimmedNombre.isBlank()) return

        viewModelScope.launch {
            val activeProfileId = profileId.firstOrNull() ?: return@launch
            val currentProfile =
                profileRepository.getProfileByIdOnce(activeProfileId) ?: return@launch

            val previousImagePath = currentImagePath ?: currentProfile.urlImagen
            val imagePath = if (newPhotoUri != null) {
                // Copia primero la nueva imagen: si falla, se conserva la anterior.
                val copiedPath = copyImageToInternalStorage(context, newPhotoUri)
                if (copiedPath != null) {
                    previousImagePath?.let { File(it).delete() }
                    copiedPath
                } else {
                    previousImagePath
                }
            } else {
                previousImagePath
            }

            profileRepository.updateProfile(
                currentProfile.copy(
                    name = trimmedNombre,
                    urlImagen = imagePath,
                    birthDate = birthDate,
                    sex = sex,
                    bloodType = bloodType,
                    allergies = allergies.normalizedOrNull(),
                    medicalNotes = medicalNotes.normalizedOrNull(),
                    healthCardNumber = healthCardNumber.normalizedOrNull(),
                    updatedAt = LocalDateTime.now()
                )
            )
            onSaved(imagePath)
        }
    }

    /**
     * Flujo con el perfil activo observado desde la sesión.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val profile: StateFlow<Profile?> =
        sessionManager.activeProfileId
            .filterNotNull()
            .flatMapLatest { id ->
                profileRepository.getProfileById(id)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    /**
     * Normaliza un texto opcional: elimina espacios y convierte los vacíos en `null`.
     */
    private fun String?.normalizedOrNull(): String? =
        this?.trim()?.takeIf { it.isNotEmpty() }
}
