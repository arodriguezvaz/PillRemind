package com.andres.pillremind.ui.screens.crearperfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andres.pillremind.data.local.database.AppDatabase
import com.andres.pillremind.data.repository.PerfilRepositoryImpl
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.ButtonSize

@Composable
fun CrearPerfilScreen(
    viewModel: CrearPerfilViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val crearPerfilFactory = remember {
        val db = AppDatabase.getInstance(context.applicationContext)
        CrearPerfilViewModelFactory(PerfilRepositoryImpl(db.perfilDao()))
    }
    val nombre = remember { mutableStateOf("") }
    val urlImagen = remember { mutableStateOf("") }
    //var editorState by remember { mutableStateOf<EditorState>(EditorState.Closed) }
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

        TextField(
            value = urlImagen.value,
            onValueChange = { urlImagen.value = it },
            label = { Text("URL de imagen") },
            modifier = Modifier
                .padding(bottom = 24.dp)
        )

        AppButton(
            text = "Guardar",
            onClick = {
                viewModel.añadirPerfil(
                    nombre = nombre.value,
                    urlImagen = urlImagen.value.ifEmpty { null }
                )
            },
            size = ButtonSize.Large,
            enabled = nombre.value.isNotBlank()
        )
    }
}
