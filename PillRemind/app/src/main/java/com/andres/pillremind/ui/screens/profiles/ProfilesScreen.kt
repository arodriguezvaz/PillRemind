package com.andres.pillremind.ui.screens.profiles

import android.R
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.ui.components.BlankCard
import com.andres.pillremind.ui.components.PerfilCard
import com.andres.pillremind.ui.screens.profile.ProfileViewModel

import com.andres.pillremind.ui.theme.PillRemindTheme


@Composable
fun ProfilesScreen(
    onAddClicked: () -> Unit,
    onProfileSelected: (Profile) -> Unit,
    modifier: Modifier = Modifier,
) {
    val profilesViewModel: ProfilesViewModel = hiltViewModel()
    val perfiles by profilesViewModel.perfiles.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier.fillMaxSize(),
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

