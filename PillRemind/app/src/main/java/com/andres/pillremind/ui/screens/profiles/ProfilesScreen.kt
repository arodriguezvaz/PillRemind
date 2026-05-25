package com.andres.pillremind.ui.screens.profiles

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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.ui.components.BlankCard
import com.andres.pillremind.ui.components.PerfilCard
import com.andres.pillremind.ui.screens.main.MainTopBar
import com.andres.pillremind.ui.screens.main.TopBarState

/**
 * Pantalla que muestra la lista de perfiles guardados.
 *
 * @param onAddClicked Callback que se ejecuta al pulsar el botón de añadir.
 * @param onProfileSelected Callback que recibe el perfil seleccionado.
 * @param modifier Modificador opcional para la pantalla.
 */
@Composable
fun ProfilesScreen(
    onAddClicked: () -> Unit,
    onProfileSelected: (Profile) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController,
    profilesViewModel: ProfilesViewModel = hiltViewModel()
) {
    val perfiles by profilesViewModel.perfiles.collectAsStateWithLifecycle()
    var topBarState by remember {
        mutableStateOf(TopBarState("Perfiles", true))
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MainTopBar(state = topBarState, navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddClicked() },
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 6.dp,
                ),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_input_add),
                    contentDescription = "Agregar profile",
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if (perfiles.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    BlankCard("No hay perfiles creados todavía :(")
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
                            profile = perfil,
                            gestionar = {
                                onProfileSelected(perfil)
                            }
                        )
                    }
                }
            }
        }
    }
}
