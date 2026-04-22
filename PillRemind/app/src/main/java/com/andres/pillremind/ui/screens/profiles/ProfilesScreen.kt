package com.andres.pillremind.ui.screens.profiles

import android.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.ui.components.PerfilCard

import com.andres.pillremind.ui.theme.PillRemindTheme


@Composable
fun ProfilesScreen(
    viewModel: ProfilesViewModel,
    onAddClicked: () -> Unit,
    onProfileSelected: (Profile) -> Unit,
    modifier: Modifier = Modifier,
) {
    val perfiles by viewModel.perfiles.collectAsStateWithLifecycle()

    Column(
        Modifier.fillMaxSize()
    ) {

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
                        profile = perfil,
                        gestionar = {
                            onProfileSelected(perfil)
                        }
                    )
                }
            }

        }
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
                painter = painterResource(id = R.drawable.ic_menu_add),
                contentDescription = "Agregar profile",
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