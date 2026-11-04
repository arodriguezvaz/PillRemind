package com.andres.pillremind.ui.screens.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.R
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.utils.copyImageToInternalStorage
import com.andres.pillremind.data.utils.deleteInternalImage
import com.andres.pillremind.domain.model.BloodType
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.model.Sex
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.notifications.ReminderNotifications
import com.andres.pillremind.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ViewModel para la gestión de perfiles.
 *
 * Permite crear, actualizar, eliminar y observar el perfil activo usando
 * [ProfileRepository] y [SessionManager].
 *
 * @property profileRepository Repositorio de perfiles.
 * @property medicationRepository Repositorio de medicamentos, usado para limpiar
 *   las imágenes de los medicamentos al borrar un perfil.
 * @property sessionManager Gestor de sesión con el perfil activo.
 * @property reminderScheduler Coordinador de alarmas de recordatorio.
 * @property notifications Gestor de notificaciones de recordatorio.
 * @property context Contexto usado para copiar y borrar imágenes.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val medicationRepository: MedicationRepository,
    private val sessionManager: SessionManager,
    private val reminderScheduler: ReminderScheduler,
    private val notifications: ReminderNotifications,
    @ApplicationContext private val context: Context
) : ViewModel() {

    /**
     * Flujo con el ID del usuario activo.
     */
    val activeUserId: Flow<Int?> = sessionManager.activeUserId

    private val _isSaving = MutableStateFlow(false)

    /** Indica que hay una operación de perfil en curso. */
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorRes = MutableStateFlow<Int?>(null)

    /** Error pendiente de mostrar en la pantalla. */
    val errorRes: StateFlow<Int?> = _errorRes.asStateFlow()

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
        if (_isSaving.value) return

        val trimmedNombre = nombre.trim()
        if (trimmedNombre.isEmpty()) {
            _errorRes.value = R.string.profile_save_name_required
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            var imagePath: String? = null

            try {
                if (photoUri != null) {
                    imagePath = copyImageToInternalStorage(context, photoUri)
                    if (imagePath == null) {
                        _errorRes.value = R.string.profile_image_error
                        return@launch
                    }
                }

                val userId = activeUserId.firstOrNull()
                if (userId == null) {
                    deleteImage(imagePath)
                    _errorRes.value = R.string.profile_save_error
                    return@launch
                }

                val saved = runCatching {
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
                }.onFailure {
                    Log.e("PillDebug", "No se pudo crear el perfil", it)
                }.isSuccess

                if (!saved) {
                    deleteImage(imagePath)
                    _errorRes.value = R.string.profile_save_error
                    return@launch
                }

                onSaved()
            } catch (e: Exception) {
                deleteImage(imagePath)
                Log.e("PillDebug", "Error inesperado al crear el perfil", e)
                _errorRes.value = R.string.profile_save_error
            } finally {
                _isSaving.value = false
            }
        }
    }

    /**
     * Elimina un perfil y limpia sus imágenes y recordatorios.
     *
     * Las imágenes solo se borran después de que Room confirme el borrado en
     * cascada; si falla, se conservan intactas.
     *
     * @param profile Perfil que se desea eliminar.
     * @param onDeleted Callback ejecutado cuando el borrado se completa.
     */
    fun deleteProfile(profile: Profile, onDeleted: () -> Unit = {}) {
        if (_isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true
            try {
                // Cancelar las alarmas antes de que Room borre las tomas en cascada.
                runCatching { reminderScheduler.cancelFutureForProfile(profile.id) }
                    .onFailure {
                        Log.e(
                            "PillDebug",
                            "No se pudieron cancelar las alarmas del perfil ${profile.id}",
                            it
                        )
                    }

                val medicationImagePaths = runCatching {
                    medicationRepository.getMedicationsOnce(profile.id)
                        .mapNotNull { it.imageUrl }
                }.getOrDefault(emptyList())

                val deleted = runCatching {
                    profileRepository.deleteProfile(profile)
                }.onFailure {
                    Log.e("PillDebug", "No se pudo borrar el perfil ${profile.id}", it)
                }.isSuccess

                if (!deleted) {
                    _errorRes.value = R.string.profile_delete_error
                    return@launch
                }

                notifications.cancelAll()
                deleteImage(profile.urlImagen)
                medicationImagePaths.forEach { path -> deleteImage(path) }

                // Solo se limpia la sesión si el perfil borrado era el activo.
                if (sessionManager.activeProfileId.firstOrNull() == profile.id) {
                    sessionManager.clearActiveProfile()
                }

                onDeleted()
            } catch (e: Exception) {
                Log.e("PillDebug", "Error inesperado al borrar el perfil", e)
                _errorRes.value = R.string.profile_delete_error
            } finally {
                _isSaving.value = false
            }
        }
    }

    val profileId = sessionManager.activeProfileId

    /**
     * Actualiza un perfil existente.
     *
     * La imagen nueva se copia antes del commit y la antigua se borra solo
     * cuando la base de datos ya está actualizada.
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
     * @param onSaved Callback ejecutado cuando el perfil ya está actualizado.
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
        onSaved: () -> Unit = {}
    ) {
        if (_isSaving.value) return

        val trimmedNombre = nombre.trim()
        if (trimmedNombre.isBlank()) {
            _errorRes.value = R.string.profile_save_name_required
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            var newImagePath: String? = null

            try {
                val activeProfileId = profileId.firstOrNull()
                if (activeProfileId == null) {
                    _errorRes.value = R.string.profile_save_error
                    return@launch
                }

                val currentProfile = profileRepository.getProfileByIdOnce(activeProfileId)
                if (currentProfile == null) {
                    _errorRes.value = R.string.profile_save_error
                    return@launch
                }

                if (newPhotoUri != null) {
                    newImagePath = copyImageToInternalStorage(context, newPhotoUri)
                    if (newImagePath == null) {
                        _errorRes.value = R.string.profile_image_error
                        return@launch
                    }
                }

                val oldImagePath = currentProfile.urlImagen
                val updated = runCatching {
                    profileRepository.updateProfile(
                        currentProfile.copy(
                            name = trimmedNombre,
                            urlImagen = newImagePath ?: currentImagePath ?: oldImagePath,
                            birthDate = birthDate,
                            sex = sex,
                            bloodType = bloodType,
                            allergies = allergies.normalizedOrNull(),
                            medicalNotes = medicalNotes.normalizedOrNull(),
                            healthCardNumber = healthCardNumber.normalizedOrNull(),
                            updatedAt = LocalDateTime.now()
                        )
                    )
                }.onFailure {
                    Log.e("PillDebug", "No se pudo actualizar el perfil $activeProfileId", it)
                }.isSuccess

                if (!updated) {
                    deleteImage(newImagePath)
                    _errorRes.value = R.string.profile_save_error
                    return@launch
                }

                if (newImagePath != null) {
                    deleteImage(oldImagePath)
                }
                onSaved()
            } catch (e: Exception) {
                deleteImage(newImagePath)
                Log.e("PillDebug", "Error inesperado al actualizar el perfil", e)
                _errorRes.value = R.string.profile_save_error
            } finally {
                _isSaving.value = false
            }
        }
    }

    /** Descarta el error actual para que no se vuelva a mostrar. */
    fun clearError() {
        _errorRes.value = null
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

    private fun deleteImage(path: String?) {
        deleteInternalImage(context, path)
    }

    /**
     * Normaliza un texto opcional: elimina espacios y convierte los vacíos en `null`.
     */
    private fun String?.normalizedOrNull(): String? =
        this?.trim()?.takeIf { it.isNotEmpty() }
}
