package com.andres.pillremind.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.andres.pillremind.R
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.AppTextField
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.components.ButtonVariant
import com.andres.pillremind.ui.components.FormField
import com.andres.pillremind.ui.screens.main.TopBarState

/**
 * Modos en los que puede estar la pantalla de perfil.
 * - `Create`: crear un perfil nuevo.
 * - `View`: ver el perfil actual.
 * - `Edit`: editar un perfil existente.
 */
sealed class ProfileMode {
    object Create : ProfileMode()
    object View : ProfileMode()
    object Edit : ProfileMode()
}

/**
 * Pantalla para crear, ver o editar un perfil.
 *
 * @param modifier Modificador opcional para la pantalla.
 * @param onSaveClicked Callback llamado tras guardar el perfil.
 * @param profileMode Modo inicial de la pantalla.
 * @param onDeletedClicked Callback llamado tras eliminar el perfil.
 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onSaveClicked: () -> Unit = {},
    profileMode: ProfileMode = ProfileMode.Create,
    onDeletedClicked: () -> Unit = {},
    onTopBarChange: (TopBarState) -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        onTopBarChange(TopBarState(title = "Ver Perfil"))
    }
    val nombre = remember { mutableStateOf("") }
    val urlImagen = remember { mutableStateOf("") }
    val perfil = profileViewModel.profile.collectAsState().value


    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    //Variable que si es true muestra el diálogo de eliminación
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // uri es null si el usuario canceló, o el Uri de la imagen seleccionada
        uri?.let {
            // Guarda o usa la URI aquí
            selectedPhotoUri = it
        }
    }

    when (profileMode) {
        ProfileMode.Create -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FormField(label = stringResource(R.string.nombre)) {
                    AppTextField(
                        value = nombre.value,
                        onValueChange = { nombre.value = it },
                        placeholder = "Nombre del perfil",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                //TODO selector de imagen de perfil
                FormField(label = stringResource(R.string.url_imagen)) {
                    AppTextField(
                        value = urlImagen.value,
                        onValueChange = { urlImagen.value = it },
                        placeholder = "URL de la imagen",
                        modifier = Modifier.padding(bottom = 24.dp),
                    )
                }
                AppButton(
                    text = "Guardar",
                    onClick = {
                        profileViewModel.addProfile(
                            nombre = nombre.value,
                            urlImagen = urlImagen.value.ifEmpty { null }
                        )
                        onSaveClicked()
                    },
                    size = ButtonSize.Large,
                    enabled = nombre.value.isNotBlank()
                )
            }
        }

        ProfileMode.Edit -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                AppTextField(
                    value = nombre.value,
                    onValueChange = { nombre.value = it },
                    placeholder = "Escribe algo...",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = urlImagen.value,
                    onValueChange = { urlImagen.value = it },
                    label = { Text("URL de imagen") },
                    modifier = Modifier
                        .padding(bottom = 24.dp),

                    )

                AppButton(
                    text = "Guardar",
                    onClick = {
                        profileViewModel.addProfile(
                            nombre = nombre.value,
                            urlImagen = urlImagen.value.ifEmpty { null }
                        )
                        onSaveClicked()
                    },
                    size = ButtonSize.Large,
                    enabled = nombre.value.isNotBlank()
                )
            }
        }

        ProfileMode.View -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Hola ${perfil?.name}")
                Row(modifier.fillMaxWidth()) {
                    //ELIMINAR
                    AppButton(
                        onClick = {
                            perfil?.let {
                                showDeleteDialog = true
                            }
                        },
                        text = stringResource(R.string.eliminar),
                        size = ButtonSize.Medium,
                        variant = ButtonVariant.Danger,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    //EDITAR
                    AppButton(
                        onClick = {},
                        text = stringResource(R.string.editar),
                        size = ButtonSize.Medium,
                        variant = ButtonVariant.Primary,
                        modifier = Modifier.weight(1f)

                    )
                }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Eliminar medicamento")
            },
            text = {
                Text("¿Seguro que quieres eliminar este medicamento?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        profileViewModel.deleteProfile(perfil!!)
                        showDeleteDialog = false
                        onDeletedClicked()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
