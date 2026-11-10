package com.andres.pillremind.ui.screens.profiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.ui.components.AppEmptyState
import com.andres.pillremind.ui.components.AppFab
import com.andres.pillremind.ui.components.PerfilCard
import com.andres.pillremind.ui.screens.main.MainTopBar
import com.andres.pillremind.ui.screens.main.TopBarAction
import com.andres.pillremind.ui.screens.main.TopBarState
import com.andres.pillremind.ui.theme.AppSpacing

/**
 * Pantalla que muestra la lista de perfiles guardados.
 *
 * @param onAddClicked Callback que se ejecuta al pulsar el botón de añadir.
 * @param onProfileSelected Callback que recibe el perfil seleccionado.
 * @param modifier Modificador opcional para la pantalla.
 * @param profilesViewModel ViewModel que gestiona la lógica de perfiles.
 * @param onSettingClicked Callback que se ejecuta al pulsar el botón de ajustes.
 * @param navController Controlador de navegación para la pantalla principal.
 * @param topLevelRoutes Rutas principales que no muestran flecha de retroceso.
 */
@Composable
fun ProfilesScreen(
    onAddClicked: () -> Unit,
    onProfileSelected: (Profile) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController,
    profilesViewModel: ProfilesViewModel = hiltViewModel(),
    onSettingClicked: () -> Unit,
    topLevelRoutes: Set<String>,
) {

    LaunchedEffect(Unit) {
        profilesViewModel.clearActiveProfile()
    }
    val uiState by profilesViewModel.uiState.collectAsStateWithLifecycle()
    val topBarState = TopBarState(
        title = stringResource(R.string.profiles_title),
        showLogo = true,
        actions = listOf(
            TopBarAction(
                icon = ImageVector.vectorResource(R.drawable.ic_settings),
                contentDescription = stringResource(R.string.settings_title),
                onClick = { onSettingClicked() }
            )
        )
    )
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MainTopBar(
                state = topBarState,
                navController = navController,
                topLevelRoutes = topLevelRoutes
            )
        },
        floatingActionButton = {
            AppFab(onClick = onAddClicked) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.profiles_add),
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.profiles.isEmpty() -> {
                    AppEmptyState(
                        icon = ImageVector.vectorResource(R.drawable.ic_person_add),
                        title = stringResource(R.string.profiles_empty_title),
                        description = stringResource(R.string.profiles_empty_body),
                        hint = stringResource(R.string.profiles_empty_hint),
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            horizontal = AppSpacing.md,
                            vertical = AppSpacing.sm,
                        ),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        items(
                            items = uiState.profiles,
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
}
