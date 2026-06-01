package com.andres.pillremind.ui.screens.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.utils.copyImageToInternalStorage
import com.andres.pillremind.di.ApplicationScope
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.domain.repository.SyncRepository
import com.andres.pillremind.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
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

/**
 * ViewModel para la gestión de perfiles.
 *
 * Permite crear, actualizar, eliminar y observar el perfil activo usando
 * [ProfileRepository] y [SessionManager].
 *
 * @property profileRepository Repositorio de perfiles.
 * @property sessionManager Gestor de sesión con el perfil activo.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val syncRepository: SyncRepository,
    @ApplicationScope private val appScope: CoroutineScope,

    @ApplicationContext private val context: Context
) : ViewModel() {

    /**
     * Flujo con el ID del usuario activo.
     */
    val activeUserId: Flow<Int?> = sessionManager.activeUserId


    private fun syncSiEsPremium() {
        appScope.launch {
            val user = userRepository.getUser() ?: return@launch
            Log.d("Sync", "esPremium=${user.esPremium}, supabaseUid=${user.supabaseUid}")
            if (user.esPremium) {
                syncRepository.syncPerfiles()
                    .onSuccess { Log.d("Sync", "Sync OK") }
                    .onFailure { Log.e("Sync", "Error: ${it.message}", it) }
            }
        }
    }

    /**
     * Agrega un nuevo perfil usando el repositorio.
     *
     * @param nombre Nombre del perfil.
     * @param photoUri URL opcional de la imagen del perfil.
     */
    fun addProfile(nombre: String, photoUri: Uri? = null) {
        val trimmedNombre = nombre.trim()
        val imagePath = photoUri?.let {
            copyImageToInternalStorage(context, it)
        }
        if (trimmedNombre.isNotEmpty()) {
            viewModelScope.launch {
                val userId = activeUserId.firstOrNull() ?: return@launch
                profileRepository.insertProfile(
                    Profile(
                        usuarioId = userId,
                        name = trimmedNombre,
                        urlImagen = imagePath
                    )
                )
                syncSiEsPremium()
            }
        }
    }

    /**
     * Elimina un perfil y limpia la sesión activa si corresponde.
     *
     * @param profile Perfil que se desea eliminar.
     */
    fun deleteProfile(profile: Profile) {
        profile.urlImagen?.let { File(it).delete() }

        // Guardar supabaseId antes de borrar de Room
        val supabaseId = profile.supabaseId

        viewModelScope.launch {
            profileRepository.deleteProfile(profile)
            sessionManager.clearActiveProfile()
        }

        // Borrar en Supabase en appScope, independiente de la navegación
        if (supabaseId != null) {
            appScope.launch {
                val user = userRepository.getUser() ?: return@launch
                if (user.esPremium) {
                    syncRepository.deleteProfile(supabaseId)
                        .onFailure { Log.e("Sync", "Error borrando perfil: ${it.message}") }
                }
            }
        }
    }

    val profileId = sessionManager.activeProfileId

    /**
     * Actualiza un perfil existente.
     *
     * @param profile Perfil con los valores actualizados.
     */
    fun updateProfile(
        nombre: String,
        newPhotoUri: Uri? = null,       // URI nueva del picker
        currentImagePath: String? = null // ruta actual guardada en Room
    ) {
        val trimmedNombre = nombre.trim()
        if (trimmedNombre.isBlank()) return

        viewModelScope.launch {
            val activeProfileId = profileId.firstOrNull() ?: return@launch
            val userId = activeUserId.firstOrNull() ?: return@launch

            val perfilActual = profileRepository.getProfileByIdOnce(activeProfileId)

            val imagePath = if (newPhotoUri != null) {
                // Borrar imagen antigua si existía
                currentImagePath?.let { File(it).delete() }
                // Copiar la nueva al almacenamiento interno
                copyImageToInternalStorage(context, newPhotoUri)
            } else {
                // Sin cambio, mantener la ruta actual
                currentImagePath
            }

            profileRepository.updateProfile(
                Profile(
                    id = activeProfileId,
                    usuarioId = userId,
                    name = trimmedNombre,
                    urlImagen = imagePath,
                    supabaseId = perfilActual?.supabaseId
                )
            )
            syncSiEsPremium()
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

}
