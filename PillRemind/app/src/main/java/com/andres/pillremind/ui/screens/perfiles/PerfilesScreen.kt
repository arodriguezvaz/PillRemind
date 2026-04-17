package com.andres.pillremind.ui.screens.perfiles

import android.R
import android.content.res.Resources
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.andres.pillremind.data.local.database.AppDatabase
import com.andres.pillremind.data.repository.PerfilRepositoryImpl
import com.andres.pillremind.domain.model.Perfil
import com.andres.pillremind.navigation.AppScreens
import com.andres.pillremind.ui.components.PerfilCard

import com.andres.pillremind.ui.theme.PillRemindTheme


@Composable
fun PerfilesScreen(
    viewModel: PerfilesViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val perfiles by viewModel.perfiles.collectAsStateWithLifecycle()

    Column() {

        if (perfiles.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text("no hay perfiles aun")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = perfiles,
                    key = { it.id }
                ) { perfil ->
                    PerfilCard(
                        perfil = perfil,
                        gestionar = {
                            // TODO NAVEGACION A VISTA DE MEDICAEMTNSO DEL PERFIL
                        }
                    )
                }
            }

        }
        FloatingActionButton(
            onClick = { navController.navigate(AppScreens.CrearPerfil.route) },
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 6.dp,
            ),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_menu_add),
                contentDescription = "Agregar perfil",
            )
        }

    }

}

@Preview
@Composable
fun TaskListEmptyScreen() {
    PillRemindTheme() {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Vista previa:conecta ViewMOdel a la app")
        }
    }
}