package com.andres.pillremind.ui.screens.profiles
import com.andres.pillremind.ui.theme.AppSpacing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.ui.components.AppFab
import com.andres.pillremind.ui.components.BlankCard
import com.andres.pillremind.ui.components.PerfilCard
import com.andres.pillremind.ui.screens.main.MainTopBar
import com.andres.pillremind.ui.screens.main.TopBarAction
import com.andres.pillremind.ui.screens.main.TopBarState

/**
 * Pantalla que muestra la lista de perfiles guardados.
 *
 * @param onAddClicked Callback que se ejecuta al pulsar el botón de añadir.
 * @param onProfileSelected Callback que recibe el perfil seleccionado.
 * @param modifier Modificador opcional para la pantalla.
 * @param profilesViewModel ViewModel que gestiona la lógica de perfiles.
 * @param onSettingClicked Callback que se ejecuta al pulsar el botón de ajustes.
 */
@Composable
fun ProfilesScreen(
    onAddClicked: () -> Unit,
    onProfileSelected: (Profile) -> Unit,
    modifier: Modifier = Modifier,
    profilesViewModel: ProfilesViewModel = hiltViewModel(),
    onSettingClicked: () -> Unit,
) {

    LaunchedEffect(Unit) {
        profilesViewModel.clearActiveProfile()
    }
    val perfiles by profilesViewModel.perfiles.collectAsStateWithLifecycle()
    val topBarState = TopBarState(
        title = stringResource(R.string.profiles_title),
        showLogo = true,
        actions = listOf(
            TopBarAction(
                icon = Icons.Rounded.Settings,
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
                canGoBack = false
            )
        },
        floatingActionButton = {
            AppFab(onClick = onAddClicked) {
                Icon(
                    imageVector = Icons.Rounded.Add,
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

            if (perfiles.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    BlankCard(stringResource(R.string.profiles_empty))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = AppSpacing.md,
                        vertical = AppSpacing.sm,
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
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
