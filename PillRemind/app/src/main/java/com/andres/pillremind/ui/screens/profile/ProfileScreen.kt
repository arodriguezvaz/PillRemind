package com.andres.pillremind.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.ButtonSize

/**
 * Clase sellada que representa el modo de profile
 */
sealed class ProfileMode {
    object Create : ProfileMode()
    object View : ProfileMode()
    object Edit : ProfileMode()
}

/**
 * Vista de creación de perfiles
 * @param profileViewModel ViewModel para la creación de perfiles
 * @param modifier Modificador para personalizar la vista
 * @param onSaveClicked Función a ejecutar al hacer clic en el botón de guardar
 * @param profileMode Modo de perfil (crear, ver o editar)
 */
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    modifier: Modifier = Modifier,
    onSaveClicked: () -> Unit = {},
    profileMode: ProfileMode = ProfileMode.Create,
) {
    val nombre = remember { mutableStateOf("") }
    val urlImagen = remember { mutableStateOf("") }
    val perfil = profileViewModel.profile.collectAsState().value
    when (profileMode) {
        ProfileMode.Create -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = nombre.value,
                    onValueChange = { nombre.value = it },
                    label = { Text("Nombre") },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
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

        ProfileMode.Edit -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = nombre.value,
                    onValueChange = { nombre.value = it },
                    label = { Text("Nombre") },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
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
            }
        }
    }
}
