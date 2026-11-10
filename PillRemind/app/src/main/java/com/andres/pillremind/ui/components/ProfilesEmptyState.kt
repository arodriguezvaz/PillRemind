package com.andres.pillremind.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.R
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme

/** Tamaño del FAB de Material 3 usado para colocar el aviso encima. */
private val FabSize = 56.dp

/** Margen que el Scaffold deja entre el FAB y el borde de la pantalla. */
private val FabMargin = 16.dp

/** Tamaño de la flecha que señala el FAB. */
private val HintIconSize = 24.dp

/** Margen derecho para centrar la flecha con el FAB. */
private val HintEndPadding = FabMargin + (FabSize - HintIconSize) / 2f

/** Separación entre el aviso y la parte superior del FAB. */
private val HintBottomPadding = FabMargin + FabSize + AppSpacing.xs

/**
 * Estado vacío de la pantalla de selección de perfiles.
 *
 * Muestra un mensaje para crear el primer perfil y un aviso con una flecha que
 * apunta al FAB de añadir. El aviso se coloca a partir de la geometría del FAB
 * para quedar justo encima de él.
 *
 * @param modifier Modificador para el estado vacío.
 */
@Composable
fun ProfilesEmptyState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = AppElevation),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_person_add),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(AppSpacing.md)
                                .size(32.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Text(
                        text = stringResource(R.string.profiles_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text(
                        text = stringResource(R.string.profiles_empty_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = AppSpacing.md,
                    end = HintEndPadding,
                    bottom = HintBottomPadding,
                ),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.profiles_empty_hint),
                modifier = Modifier.weight(1f, fill = false),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
            )
            Spacer(modifier = Modifier.width(AppSpacing.xxs))
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_downward),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(HintIconSize),
            )
        }
    }
}

@Preview(showBackground = true, name = "Perfiles vacío")
@Composable
private fun ProfilesEmptyStatePreview() {
    PillRemindTheme {
        ProfilesEmptyState(modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true, fontScale = 1.5f, name = "Perfiles vacío oscuro")
@Composable
private fun ProfilesEmptyStateDarkPreview() {
    PillRemindTheme(darkTheme = true) {
        ProfilesEmptyState(modifier = Modifier.fillMaxSize())
    }
}
