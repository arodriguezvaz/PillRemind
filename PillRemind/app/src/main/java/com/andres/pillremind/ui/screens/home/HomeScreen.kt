package com.andres.pillremind.ui.screens.home

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.ui.components.MedicationCard


@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onAddClicked: () -> Unit,
) {
    val perfil = homeViewModel.profile.collectAsState().value
    val medications by homeViewModel.medications.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (perfil != null) {
            Text(text = "Hola ${perfil.name}")
        } else {
            Text(text = "Cargando...")
        }
        if (medications.isEmpty()) {
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
                    items = medications,
                    key = { it.id }
                ) { medication ->
                    MedicationCard(
                        medication = medication,
                        tomar = {
                            //TODO
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