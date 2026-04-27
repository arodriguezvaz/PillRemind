package com.andres.pillremind.ui.screens.medications

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.ui.components.BlankCard
import com.andres.pillremind.ui.components.MedicationCard
import com.andres.pillremind.ui.screens.home.HomeViewModel

@Composable
fun MedicationsScreen(
    medicationsViewModel: MedicationsViewModel,
    modifier: Modifier = Modifier,
    onAddClicked: () -> Unit,
) {
    val medications by medicationsViewModel.medications.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {


        if (medications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(), // importante
                contentAlignment = Alignment.Center
            ) {
                BlankCard("No hay medicamentos aún")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(), // importante
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = medications,
                    key = { it.id }
                ) { medication ->
                    MedicationCard(
                        medication = medication,
                        tomar = {
                            // TODO
                        }
                    )
                }
            }
        }
    }
}