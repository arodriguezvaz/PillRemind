package com.andres.pillremind.ui.screens.profile

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import com.andres.pillremind.ui.components.AppPhotoPicker
import com.andres.pillremind.ui.components.AppTextField
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.components.ButtonVariant
import com.andres.pillremind.ui.components.FormField
import com.andres.pillremind.ui.screens.main.TopBarState
import java.io.File

sealed class ProfileMode {
    object Create : ProfileMode()
    object View : ProfileMode()
    object Edit : ProfileMode()
}

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

    val name = remember { mutableStateOf("") }
    val perfil = profileViewModel.profile.collectAsState().value
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentMode by remember { mutableStateOf(profileMode) }


    LaunchedEffect(perfil) {
        perfil?.let {
            name.value = it.name
            selectedPhotoUri = it.urlImagen?.let(Uri::parse)
        }
    }
    when (currentMode) {
        //CREATE
        ProfileMode.Create -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppPhotoPicker(
                    photoUri = selectedPhotoUri,
                    onPhotoSelected = { selectedPhotoUri = it },
                    enabled = true,
                    placeholderIcon = Icons.Default.AddAPhoto,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                FormField(label = stringResource(R.string.nombre)) {
                    AppTextField(
                        value = name.value,
                        onValueChange = { name.value = it },
                        placeholder = stringResource(R.string.añadir_perfil),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                AppButton(
                    text = "Guardar",
                    onClick = {
                        profileViewModel.addProfile(
                            nombre = name.value,
                            photoUri = selectedPhotoUri
                        )
                        onSaveClicked()
                    },
                    size = ButtonSize.Large,
                    enabled = name.value.isNotBlank()
                )
            }
        }
        //EDIT
        ProfileMode.Edit -> {
            var currentImagePath by remember { mutableStateOf(perfil?.urlImagen) }
            var newPhotoUri by remember { mutableStateOf<Uri?>(null) }

            LaunchedEffect(perfil) {
                perfil?.name?.let { name.value = it }
                currentImagePath = perfil?.urlImagen
            }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppPhotoPicker(
                    photoUri = newPhotoUri ?: currentImagePath?.let { Uri.fromFile(File(it)) },
                    onPhotoSelected = { newPhotoUri = it }, // solo actualiza la nueva
                    enabled = true,
                    placeholderIcon = Icons.Default.AddAPhoto,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                FormField(label = stringResource(R.string.nombre)) {
                    AppTextField(
                        value = name.value,
                        onValueChange = { name.value = it },
                        placeholder = "Nombre del perfil",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                AppButton(
                    text = "Guardar",
                    onClick = {
                        profileViewModel.updateProfile(
                            nombre = name.value,
                            newPhotoUri = newPhotoUri,
                            currentImagePath = currentImagePath
                        )
                        onSaveClicked()
                        currentMode = ProfileMode.View
                    },
                    size = ButtonSize.Large,
                    enabled = name.value.isNotBlank()
                )
            }
        }

        ProfileMode.View -> {
            LaunchedEffect(perfil) {
                perfil?.urlImagen?.let { selectedPhotoUri = Uri.parse(it) }
            }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppPhotoPicker(
                    photoUri = selectedPhotoUri,
                    onPhotoSelected = {},
                    enabled = false,
                    placeholderIcon = Icons.Default.Person,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                FormField(label = stringResource(R.string.nombre)) {
                    AppTextField(
                        value = name.value,
                        onValueChange = { name.value = it },
                        placeholder = "Nombre del perfil",
                        modifier = Modifier.padding(bottom = 16.dp),
                        enabled = false
                    )
                }

                Row(modifier.fillMaxWidth()) {
                    AppButton(
                        onClick = { perfil?.let { showDeleteDialog = true } },
                        text = stringResource(R.string.eliminar),
                        size = ButtonSize.Medium,
                        variant = ButtonVariant.Danger,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    AppButton(
                        onClick = { currentMode = ProfileMode.Edit },
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
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar perfil") },
            text = { Text("¿Seguro que quieres eliminar este perfil?") },
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
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}